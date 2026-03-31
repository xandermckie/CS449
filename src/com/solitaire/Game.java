package com.solitaire;

/**
 * Abstract base class for all Peg Solitaire game modes.
 *
 * <p>This class defines the common structure and behavior shared between
 * ManualGame and AutomatedGame, satisfying the Sprint 3 class hierarchy
 * requirement. It owns the Board and provides shared operations such as
 * move validation, game-over detection, performance rating, and board
 * randomization. Subclasses implement mode-specific move behavior.
 *
 * <p>Refactored based on Deepseek's design analysis: Game.randomize() now
 * delegates entirely to Board.randomizeHoles(), following the "Tell, Don't Ask"
 * principle. Board controls its own internal state — Game only decides when
 * to randomize, not how.
 *
 * <p>User Story 3: As a player, I want to start a new game of the chosen
 * board size and game mode so that I can play with my preferred settings.
 *
 * <p>User Story 8: As a player, I want to randomize the state of the board
 * during a manual game so that I can explore different positions.
 */
public abstract class Game {

    protected final Board board;

    // ── Constructor ──────────────────────────────────────────────────────────────

    /**
     * Initialises a game with the given board size and type.
     *
     * @param size board size (odd, >= 3)
     * @param type board shape
     */
    protected Game(int size, BoardType type) {
        this.board = new Board(size, type);
    }

    // ── Common public API ────────────────────────────────────────────────────────

    /** Returns the underlying board. */
    public Board getBoard() { return board; }

    /** Returns true if no valid moves remain. */
    public boolean isGameOver() { return board.isGameOver(); }

    /** Returns the number of pegs currently on the board. */
    public int getPegCount() { return board.getPegCount(); }

    /** Returns the performance rating based on remaining pegs. */
    public String getPerformanceRating() { return board.getPerformanceRating(); }

    /**
     * Randomizes the board state by delegating to Board.randomizeHoles().
     *
     * <p>Following Deepseek's "Tell, Don't Ask" recommendation: Game decides
     * WHEN to randomize; Board decides HOW — keeping Board's internal grid
     * fully encapsulated. No external loop or setHole() calls needed here.
     *
     * <p>User Story 8: AC 8.1 — Board state changes; peg count stays the same.
     * AC 8.2 — At least one valid move remains after randomization.
     */
    public void randomize() {
        board.randomizeHoles();  // Board controls its own redistribution
        board.resetGameOver();   // Allow play to continue after randomization
    }

    // ── Abstract methods — subclasses define mode-specific behaviour ─────────────

    /**
     * Executes a single move in this game mode.
     * ManualGame: validates and applies a player-specified move.
     * AutomatedGame: selects and applies the next move automatically.
     *
     * @return true if a move was made
     */
    public abstract boolean makeMove(int fromRow, int fromCol, int toRow, int toCol);

    /**
     * Returns a display name for this game mode (e.g. "Manual", "Automated").
     */
    public abstract String getModeName();

}