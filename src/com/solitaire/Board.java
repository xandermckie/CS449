package com.solitaire;

/**
 * Core game logic for Peg Solitaire — separated from the UI layer per Sprint 2 requirements.
 *
 * <p>User Story 1 (corrected from Deepseek output):
 * As a player, I want to select the board size (5, 7, or 9) before starting a game
 * so that I can customize the scale and duration of the puzzle to match my preference.
 *
 * <p>User Story 4 (corrected from Deepseek output):
 * As a player, I want to make a move by jumping one of my pegs over an adjacent peg
 * into an empty hole so that the jumped-over peg is removed from the board, allowing
 * me to progress toward winning the game.
 *
 * <p>User Story 5:
 * As a player, I want the game to automatically end when no valid moves remain so
 * that I know the game is complete and can see my final peg count.
 */
public class Board {

    // Orthogonal jump directions: {rowDelta, colDelta}
    private static final int[][] DIRECTIONS = { {-1, 0}, {1, 0}, {0, -1}, {0, 1} };

    private final HoleState[][] grid;
    private final int size;
    private final BoardType type;
    private boolean gameOver;

    // ── Constructor ──────────────────────────────────────────────────────────────

    /**
     * Creates a new board with the given size and type, initialised to the standard
     * starting position: all valid holes filled with pegs except the centre hole.
     *
     * <p>AC 1.1: Given the player has selected a board size (e.g. 5, 7, or 9) and a
     * board type (English, Hexagon, or Diamond) using the UI controls,
     * When they click the "New Game" button,
     * Then the board is initialised with the correct initial peg placement for that
     * combination (all holes filled except the centre hole, following the standard
     * rules of the selected board type).
     *
     * @param size board size (must be an odd number >= 3)
     * @param type board shape type
     * @throws IllegalArgumentException if size is even or less than 3
     */
    public Board(int size, BoardType type) {
        if (size < 3 || size % 2 == 0) {
            throw new IllegalArgumentException(
                    "Board size must be an odd number >= 3, got: " + size);
        }
        this.size     = size;
        this.type     = type;
        this.gameOver = false;
        this.grid     = new HoleState[size][size];
        initialiseBoard();
    }

    // ── Public API ───────────────────────────────────────────────────────────────

    public int getSize()         { return size; }
    public BoardType getType()   { return type; }
    public boolean isGameOver()  { return gameOver; }

    /**
     * Returns the state of the hole at (row, col).
     */
    public HoleState getHole(int row, int col) {
        return grid[row][col];
    }

    /**
     * Returns the total number of pegs currently on the board.
     */
    public int getPegCount() {
        int count = 0;
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                if (grid[r][c] == HoleState.PEG) count++;
        return count;
    }

    /**
     * Attempts to move the peg at (fromRow, fromCol) by jumping over the adjacent
     * peg to the empty hole at (toRow, toCol). The jumped-over peg is removed.
     *
     * <p>AC 4.1: Given a game in progress with at least one valid move remaining,
     * When the player performs a move that results in no further valid moves on the board,
     * Then the game automatically ends and displays a notification informing the player
     * that no more moves are possible.
     *
     * @return true if the move was valid and executed; false otherwise
     */
    public boolean tryMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (!isValidMove(fromRow, fromCol, toRow, toCol)) return false;

        int midRow = (fromRow + toRow) / 2;
        int midCol = (fromCol + toCol) / 2;

        grid[fromRow][fromCol] = HoleState.EMPTY;
        grid[midRow][midCol]   = HoleState.EMPTY;   // jumped peg removed from board
        grid[toRow][toCol]     = HoleState.PEG;

        if (!hasValidMoves()) gameOver = true;

        return true;
    }

    /**
     * Returns true if the described jump is a legal move.
     * Rules: source must have a peg, destination must be empty and valid,
     * exactly two orthogonal steps apart, and the middle hole must contain a peg.
     */
    public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (!inBounds(fromRow, fromCol) || !inBounds(toRow, toCol)) return false;
        if (grid[fromRow][fromCol] != HoleState.PEG)                return false;
        if (grid[toRow][toCol]     != HoleState.EMPTY)              return false;

        int dr = toRow - fromRow;
        int dc = toCol - fromCol;

        // Must be exactly 2 steps in one orthogonal direction
        boolean orthogonal2 = (Math.abs(dr) == 2 && dc == 0)
                || (dr == 0 && Math.abs(dc) == 2);
        if (!orthogonal2) return false;

        int midRow = (fromRow + toRow) / 2;
        int midCol = (fromCol + toCol) / 2;
        return grid[midRow][midCol] == HoleState.PEG;
    }

    /**
     * Returns true if at least one valid move exists anywhere on the board.
     *
     * <p>AC 5.1: Given a game in progress with exactly one possible move remaining,
     * When the player executes that final move,
     * Then the game immediately detects that no further moves are possible and
     * displays a "Game Over" message.
     */
    public boolean hasValidMoves() {
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                if (grid[r][c] == HoleState.PEG)
                    for (int[] d : DIRECTIONS)
                        if (isValidMove(r, c, r + d[0] * 2, c + d[1] * 2))
                            return true;
        return false;
    }

    /**
     * Returns a performance rating string based on remaining peg count.
     *
     * <p>Ratings: 1 peg = Outstanding, 2 pegs = Very Good, 3 pegs = Good, 4+ = Average.
     *
     * <p>AC 4.2: Given a game has just ended with exactly 2 pegs left on the board,
     * When the game over notification appears,
     * Then the notification includes the performance rating "Very Good" (as defined
     * by the rating system: 2 pegs = Very Good) and presents the player with an option
     * to start a new game or return to the main menu.
     *
     * <p>AC 5.2: Given the game has just ended with 3 pegs remaining on the board,
     * When the "Game Over" message is displayed,
     * Then the message includes the player's final peg count (3) and the corresponding
     * performance rating ("Good").
     */
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
        // Mark all cells invalid first
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                grid[r][c] = HoleState.INVALID;

        switch (type) {
            case ENGLISH:  initialiseEnglish();  break;
            case HEXAGON:  initialiseHexagon();  break;
            case DIAMOND:  initialiseDiamond();  break;
        }

        // Standard starting position: centre hole is empty
        grid[size / 2][size / 2] = HoleState.EMPTY;
    }

    /**
     * English board: plus / cross shape.
     * The inner-third band forms a full-width cross.
     */
    private void initialiseEnglish() {
        int third = size / 3;
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                if ((r >= third && r < size - third) || (c >= third && c < size - third))
                    grid[r][c] = HoleState.PEG;
    }

    /**
     * Hexagon board: widest at the centre row, symmetric narrowing above and below.
     */
    private void initialiseHexagon() {
        int centre = size / 2;
        for (int r = 0; r < size; r++) {
            int offset   = Math.abs(r - centre);
            int colStart = offset / 2;
            int colEnd   = size - 1 - colStart;
            for (int c = colStart; c <= colEnd; c++)
                grid[r][c] = HoleState.PEG;
        }
    }

    /**
     * Diamond board: rhombus / diamond shape centred on the grid.
     */
    private void initialiseDiamond() {
        int centre = size / 2;
        for (int r = 0; r < size; r++) {
            int offset = Math.abs(r - centre);
            for (int c = offset; c <= size - 1 - offset; c++)
                grid[r][c] = HoleState.PEG;
        }
    }
}