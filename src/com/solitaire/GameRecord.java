package com.solitaire;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Records a Peg Solitaire game to a plain-text file and reads it back for replay.
 *
 * File format:
 *   SIZE:7
 *   TYPE:ENGLISH
 *   MODE:Manual
 *   MOVE:3,1,3,3
 *   RANDOMIZE:PEG,PEG,EMPTY,...   (full board snapshot so replay is exact)
 *   MOVE:5,3,3,3
 *   END:4:Good
 */
public class GameRecord {

    // ── Inner class ───────────────────────────────────────────────────────────────

    public static class RecordedAction {
        public enum Type { MOVE, RANDOMIZE, END }

        public final Type   type;
        public final int    fromRow, fromCol, toRow, toCol;
        public final int    finalPegCount;
        public final String rating;
        /** Full board snapshot after a RANDOMIZE — comma-separated HoleState names, row by row. */
        public final String boardSnapshot;

        /** MOVE constructor */
        public RecordedAction(int fromRow, int fromCol, int toRow, int toCol) {
            this.type          = Type.MOVE;
            this.fromRow       = fromRow;
            this.fromCol       = fromCol;
            this.toRow         = toRow;
            this.toCol         = toCol;
            this.finalPegCount = 0;
            this.rating        = null;
            this.boardSnapshot = null;
        }

        /** RANDOMIZE constructor — stores the exact board state so replay is deterministic */
        public RecordedAction(String boardSnapshot) {
            this.type          = Type.RANDOMIZE;
            this.fromRow       = 0;
            this.fromCol       = 0;
            this.toRow         = 0;
            this.toCol         = 0;
            this.finalPegCount = 0;
            this.rating        = null;
            this.boardSnapshot = boardSnapshot;
        }

        /** END constructor */
        public RecordedAction(int finalPegCount, String rating) {
            this.type          = Type.END;
            this.fromRow       = 0;
            this.fromCol       = 0;
            this.toRow         = 0;
            this.toCol         = 0;
            this.finalPegCount = finalPegCount;
            this.rating        = rating;
            this.boardSnapshot = null;
        }
    }

    // ── Fields ────────────────────────────────────────────────────────────────────

    private final int       boardSize;
    private final BoardType boardType;
    private final String    modeName;
    private final List<RecordedAction> actions = new ArrayList<>();

    // ── Constructor ───────────────────────────────────────────────────────────────

    public GameRecord(int boardSize, BoardType boardType, String modeName) {
        this.boardSize = boardSize;
        this.boardType = boardType;
        this.modeName  = modeName;
    }

    // ── Recording ─────────────────────────────────────────────────────────────────

    /** Records a move. */
    public void recordMove(int fromRow, int fromCol, int toRow, int toCol) {
        actions.add(new RecordedAction(fromRow, fromCol, toRow, toCol));
    }

    /**
     * Records a randomize event by snapshotting the entire board state.
     * This ensures replay restores the exact same layout rather than re-randomizing
     * with a different random seed.
     *
     * @param board the board immediately after randomization
     */
    public void recordRandomize(Board board) {
        StringBuilder sb = new StringBuilder();
        int size = board.getSize();
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (sb.length() > 0) sb.append(",");
                sb.append(board.getHole(r, c).name());
            }
        }
        actions.add(new RecordedAction(sb.toString()));
    }

    /** Records the game-over state. */
    public void recordEnd(int finalPegCount, String rating) {
        actions.add(new RecordedAction(finalPegCount, rating));
    }

    /** Returns the number of recorded actions. */
    public int getActionCount() { return actions.size(); }

    // ── Saving ────────────────────────────────────────────────────────────────────

    public void saveToFile(String filePath) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
            pw.println("# PegSolitaire Recording");
            pw.println("# Date: " + LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            pw.println("SIZE:" + boardSize);
            pw.println("TYPE:" + boardType.name());
            pw.println("MODE:" + modeName);

            for (RecordedAction a : actions) {
                switch (a.type) {
                    case MOVE:
                        pw.println("MOVE:" + a.fromRow + "," + a.fromCol
                                + "," + a.toRow + "," + a.toCol);
                        break;
                    case RANDOMIZE:
                        pw.println("RANDOMIZE:" + a.boardSnapshot);
                        break;
                    case END:
                        pw.println("END:" + a.finalPegCount + ":" + a.rating);
                        break;
                }
            }
        }
    }

    // ── Loading ───────────────────────────────────────────────────────────────────

    public static GameRecord loadFromFile(String filePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            int        size   = 7;
            BoardType  type   = BoardType.ENGLISH;
            String     mode   = "Manual";
            GameRecord record = null;

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                if (line.startsWith("SIZE:")) {
                    size = Integer.parseInt(line.substring(5));
                } else if (line.startsWith("TYPE:")) {
                    type = BoardType.valueOf(line.substring(5));
                } else if (line.startsWith("MODE:")) {
                    mode   = line.substring(5);
                    record = new GameRecord(size, type, mode);
                } else if (record != null && line.startsWith("MOVE:")) {
                    String[] p = line.substring(5).split(",");
                    record.actions.add(new RecordedAction(
                            Integer.parseInt(p[0]), Integer.parseInt(p[1]),
                            Integer.parseInt(p[2]), Integer.parseInt(p[3])));
                } else if (record != null && line.startsWith("RANDOMIZE:")) {
                    // Load board snapshot so replay is exact
                    record.actions.add(new RecordedAction(line.substring(10)));
                } else if (record != null && line.equals("RANDOMIZE")) {
                    // Legacy: old files without snapshot — just mark as randomize
                    record.actions.add(new RecordedAction(""));
                } else if (record != null && line.startsWith("END:")) {
                    String[] p = line.substring(4).split(":", 2);
                    record.actions.add(new RecordedAction(
                            Integer.parseInt(p[0]), p.length > 1 ? p[1] : "Average"));
                }
            }
            if (record == null)
                throw new IOException("Invalid recording file: missing MODE line");
            return record;
        }
    }

    // ── Accessors ─────────────────────────────────────────────────────────────────

    public int getBoardSize()                   { return boardSize; }
    public BoardType getBoardType()             { return boardType; }
    public String getModeName()                 { return modeName; }
    public List<RecordedAction> getActions()    { return actions; }
}