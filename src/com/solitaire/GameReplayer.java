package com.solitaire;

import java.util.List;

/**
 * Replays a recorded Peg Solitaire game action by action.
 *
 * For RANDOMIZE actions, the exact board snapshot stored in the recording
 * is restored directly — no re-randomization occurs, so replay is deterministic.
 */
public class GameReplayer {

    private final GameRecord                       record;
    private final Game                             game;
    private final List<GameRecord.RecordedAction>  actions;
    private int                                    currentIndex;

    // ── Constructor ───────────────────────────────────────────────────────────────

    public GameReplayer(GameRecord record) {
        this.record       = record;
        this.actions      = record.getActions();
        this.currentIndex = 0;

        if ("Automated".equals(record.getModeName())) {
            this.game = new AutomatedGame(record.getBoardSize(), record.getBoardType());
        } else {
            this.game = new ManualGame(record.getBoardSize(), record.getBoardType());
        }
    }

    // ── Playback API ──────────────────────────────────────────────────────────────

    public Game  getGame()   { return game; }
    public Board getBoard()  { return game.getBoard(); }

    public boolean hasNext() {
        return currentIndex < actions.size() &&
                actions.get(currentIndex).type != GameRecord.RecordedAction.Type.END;
    }

    public boolean isFinished()     { return !hasNext(); }
    public int getCurrentIndex()    { return currentIndex; }
    public int getTotalActions()    { return actions.size(); }

    /**
     * Applies the next recorded action and advances the pointer.
     *
     * MOVE     — delegates to game.makeMove()
     * RANDOMIZE — restores the exact board snapshot from the recording
     * END      — no-op, signals replay is complete
     */
    public GameRecord.RecordedAction stepForward() {
        if (!hasNext()) return null;

        GameRecord.RecordedAction action = actions.get(currentIndex++);

        switch (action.type) {
            case MOVE:
                game.makeMove(action.fromRow, action.fromCol,
                        action.toRow,  action.toCol);
                break;

            case RANDOMIZE:
                // Restore the exact board state recorded — do NOT re-randomize
                if (action.boardSnapshot != null && !action.boardSnapshot.isEmpty()) {
                    restoreBoardSnapshot(action.boardSnapshot);
                }
                // Reset gameOver flag so replay can continue after restoring
                game.getBoard().resetGameOver();
                break;

            case END:
                break;
        }
        return action;
    }

    public void playToEnd() {
        while (hasNext()) stepForward();
    }

    public int getFinalPegCount() {
        for (GameRecord.RecordedAction a : actions)
            if (a.type == GameRecord.RecordedAction.Type.END) return a.finalPegCount;
        return game.getPegCount();
    }

    public String getFinalRating() {
        for (GameRecord.RecordedAction a : actions)
            if (a.type == GameRecord.RecordedAction.Type.END) return a.rating;
        return game.getPerformanceRating();
    }

    // ── Private helpers ───────────────────────────────────────────────────────────

    /**
     * Restores the board to the exact state captured in the snapshot string.
     * The snapshot is a comma-separated list of HoleState names, row by row.
     */
    private void restoreBoardSnapshot(String snapshot) {
        Board  board  = game.getBoard();
        int    size   = board.getSize();
        String[] vals = snapshot.split(",");
        int idx = 0;
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (idx < vals.length) {
                    try {
                        HoleState state = HoleState.valueOf(vals[idx]);
                        // Only set non-INVALID holes so the board shape is preserved
                        if (board.getHole(r, c) != HoleState.INVALID) {
                            board.setHole(r, c, state);
                        }
                    } catch (IllegalArgumentException ignored) {}
                }
                idx++;
            }
        }
    }
}