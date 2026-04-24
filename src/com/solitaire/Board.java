package com.solitaire;

/**
 * Core game logic for Peg Solitaire — separated from the UI layer.
 *
 * <p>User Story 1: As a player, I want to select the board size (5, 7, or 9)
 * before starting a game so that I can customize the scale and duration of
 * the puzzle to match my preference.
 *
 * <p>User Story 4: As a player, I want to make a move by jumping one of my
 * pegs over an adjacent peg into an empty hole so that the jumped-over peg
 * is removed from the board, allowing me to progress toward winning the game.
 */
public class Board {

    private static final int[][] DIRECTIONS            = { {-1,0}, {1,0}, {0,-1}, {0,1} };
    private static final int     MAX_RANDOMIZE_ATTEMPTS = 100;

    private final HoleState[][] grid;
    private final int size;
    private final BoardType type;
    private boolean gameOver;

    // ── Constructor ──────────────────────────────────────────────────────────────

    public Board(int size, BoardType type) {
        if (size < 3 || size % 2 == 0)
            throw new IllegalArgumentException(
                    "Board size must be an odd number >= 3, got: " + size);
        this.size     = size;
        this.type     = type;
        this.gameOver = false;
        this.grid     = new HoleState[size][size];
        initialiseBoard();
    }

    // ── Public API ───────────────────────────────────────────────────────────────

    public int getSize()        { return size; }
    public BoardType getType()  { return type; }
    public boolean isGameOver() { return gameOver; }

    public HoleState getHole(int row, int col) { return grid[row][col]; }

    /**
     * Sets the state of a hole directly. Only affects non-INVALID holes.
     *
     * Used by:
     *   - Game.randomizeHoles() to redistribute pegs
     *   - GameReplayer.restoreBoardSnapshot() to replay randomize actions exactly
     */
    public void setHole(int row, int col, HoleState state) {
        if (row >= 0 && row < size && col >= 0 && col < size
                && grid[row][col] != HoleState.INVALID) {
            grid[row][col] = state;
        }
    }

    /**
     * Shuffles pegs among valid holes while preserving the current peg count.
     * Retries until at least one valid move exists so the game stays playable.
     *
     * <p>Following Deepseek's "Tell, Don't Ask" recommendation: Board controls
     * its own internal state — Game.randomize() delegates here entirely.
     *
     * <p>User Story 8: AC 8.1 — peg count unchanged. AC 8.2 — valid move remains.
     */
    public void randomizeHoles() {
        java.util.List<int[]> validHoles = new java.util.ArrayList<>();
        int pegCount = 0;
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (grid[r][c] == HoleState.PEG || grid[r][c] == HoleState.EMPTY) {
                    validHoles.add(new int[]{r, c});
                    if (grid[r][c] == HoleState.PEG) pegCount++;
                }
            }
        }

        // Guard: nothing to shuffle if no pegs exist
        if (pegCount == 0) return;

        java.util.Random rng = new java.util.Random();
        int attempts = 0;
        do {
            for (int i = validHoles.size() - 1; i > 0; i--) {
                int j = rng.nextInt(i + 1);
                int[] tmp = validHoles.get(i);
                validHoles.set(i, validHoles.get(j));
                validHoles.set(j, tmp);
            }
            for (int i = 0; i < validHoles.size(); i++) {
                int[] pos = validHoles.get(i);
                grid[pos[0]][pos[1]] = (i < pegCount) ? HoleState.PEG : HoleState.EMPTY;
            }
            attempts++;
        } while (!hasValidMoves() && attempts < MAX_RANDOMIZE_ATTEMPTS);
    }

    /**
     * Resets the gameOver flag after randomization or snapshot restore
     * so that play can continue.
     */
    public void resetGameOver() { gameOver = false; }

    public int getPegCount() {
        int count = 0;
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                if (grid[r][c] == HoleState.PEG) count++;
        return count;
    }

    public boolean tryMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (!isValidMove(fromRow, fromCol, toRow, toCol)) return false;

        int midRow = (fromRow + toRow) / 2;
        int midCol = (fromCol + toCol) / 2;

        grid[fromRow][fromCol] = HoleState.EMPTY;
        grid[midRow][midCol]   = HoleState.EMPTY;
        grid[toRow][toCol]     = HoleState.PEG;

        if (!hasValidMoves()) gameOver = true;
        return true;
    }

    public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (!inBounds(fromRow, fromCol) || !inBounds(toRow, toCol)) return false;
        if (grid[fromRow][fromCol] != HoleState.PEG)               return false;
        if (grid[toRow][toCol]     != HoleState.EMPTY)             return false;

        int dr = toRow - fromRow, dc = toCol - fromCol;
        boolean orthogonal2 = (Math.abs(dr) == 2 && dc == 0)
                || (dr == 0 && Math.abs(dc) == 2);
        if (!orthogonal2) return false;

        return grid[(fromRow+toRow)/2][(fromCol+toCol)/2] == HoleState.PEG;
    }

    public boolean hasValidMoves() {
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                if (grid[r][c] == HoleState.PEG)
                    for (int[] d : DIRECTIONS)
                        if (isValidMove(r, c, r+d[0]*2, c+d[1]*2))
                            return true;
        return false;
    }

    public String getPerformanceRating() {
        int pegs = getPegCount();
        if (pegs == 1) return "Outstanding";
        if (pegs == 2) return "Very Good";
        if (pegs == 3) return "Good";
        return "Average";
    }

    // ── Private helpers ──────────────────────────────────────────────────────────

    private boolean inBounds(int r, int c) {
        return r >= 0 && r < size && c >= 0 && c < size
                && grid[r][c] != HoleState.INVALID;
    }

    private void initialiseBoard() {
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                grid[r][c] = HoleState.INVALID;

        switch (type) {
            case ENGLISH:  initialiseEnglish();  break;
            case HEXAGON:  initialiseHexagon();  break;
            case DIAMOND:  initialiseDiamond();  break;
        }
        grid[size/2][size/2] = HoleState.EMPTY;
    }

    private void initialiseEnglish() {
        int third = size / 3;
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                if ((r >= third && r < size-third) || (c >= third && c < size-third))
                    grid[r][c] = HoleState.PEG;
    }

    private void initialiseHexagon() {
        int centre = size / 2;
        for (int r = 0; r < size; r++) {
            int offset   = Math.abs(r - centre);
            int colStart = offset / 2;
            int colEnd   = size - 1 - colStart;
            for (int c = colStart; c <= colEnd; c++) grid[r][c] = HoleState.PEG;
        }
    }

    private void initialiseDiamond() {
        int centre = size / 2;
        for (int r = 0; r < size; r++) {
            int offset = Math.abs(r - centre);
            for (int c = offset; c <= size-1-offset; c++) grid[r][c] = HoleState.PEG;
        }
    }
}