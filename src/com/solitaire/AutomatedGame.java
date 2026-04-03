package com.solitaire;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Automated game mode — the computer selects and executes moves automatically.
 *
 * Sprint 4 addition: getLastMove() lets GUI record each automated move.
 */
public class AutomatedGame extends Game {

    private static final Random RNG = new Random();
    private int[] lastMove = null;

    public AutomatedGame(int size, BoardType type) {
        super(size, type);
    }

    /**
     * Selects a random valid move, executes it, and stores it in lastMove.
     * GUI calls getLastMove() afterwards to record it.
     */
    @Override
    public boolean makeMove(int fromRow, int fromCol, int toRow, int toCol) {
        List<int[]> moves = getAllValidMoves();
        if (moves.isEmpty()) { lastMove = null; return false; }
        int[] chosen = moves.get(RNG.nextInt(moves.size()));
        boolean result = board.tryMove(chosen[0], chosen[1], chosen[2], chosen[3]);
        lastMove = result ? chosen : null;
        return result;
    }

    /** Convenience wrapper — picks and executes one automated move. */
    public boolean makeAutoMove() {
        return makeMove(-1, -1, -1, -1);
    }

    /**
     * Returns the last move made as {fromRow, fromCol, toRow, toCol}, or null.
     * Used by GUI to record automated moves without coupling AutomatedGame to GameRecord.
     */
    public int[] getLastMove() { return lastMove; }

    /** Returns all valid moves currently available on the board. */
    public List<int[]> getAllValidMoves() {
        List<int[]> moves = new ArrayList<>();
        int size = board.getSize();
        int[][] dirs = {{-2,0},{2,0},{0,-2},{0,2}};
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                if (board.getHole(r, c) == HoleState.PEG)
                    for (int[] d : dirs)
                        if (board.isValidMove(r, c, r+d[0], c+d[1]))
                            moves.add(new int[]{r, c, r+d[0], c+d[1]});
        return moves;
    }

    @Override
    public String getModeName() { return "Automated"; }
}