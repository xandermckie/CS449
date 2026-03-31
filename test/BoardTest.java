package com.solitaire;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests — contains the original Sprint 0 arithmetic tests plus
 * Sprint 3 tests clearly divided into Manual Game and Automated Game sections.
 *
 * The Manual Game and Automated Game sections are the ones to show
 * in the Sprint 3 video demonstration for parts (e) and (f).
 */
class UnitTests {

    // ════════════════════════════════════════════════════════════════════════════
    // Original Sprint 0 tests — kept unchanged
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * User Story: As a developer, I want to ensure that basic arithmetic
     * operations work correctly so that the game logic calculations are accurate.
     *
     * Acceptance Criteria:
     * - Addition should correctly sum two numbers
     * - Subtraction should correctly find the difference
     * - Result should be accurate for both positive and negative numbers
     */
    @Test
    void testBasicArithmetic() {
        Calculator calc = new Calculator();

        assertEquals(10, calc.add(7, 3),
                "Adding 7 and 3 should equal 10");
        assertEquals(0, calc.add(-5, 5),
                "Adding -5 and 5 should equal 0");
        assertEquals(4, calc.subtract(7, 3),
                "Subtracting 3 from 7 should equal 4");
        assertEquals(-8, calc.subtract(2, 10),
                "Subtracting 10 from 2 should equal -8");
    }

    /**
     * User Story: As a developer, I want to validate input data properly
     * so that the application handles edge cases without crashing.
     *
     * Acceptance Criteria:
     * - Division by zero should throw an exception
     * - Negative numbers should be handled correctly
     * - Valid operations should complete successfully
     */
    @Test
    void testInputValidation() {
        Calculator calc = new Calculator();

        assertEquals(4, calc.divide(12, 3),
                "Dividing 12 by 3 should equal 4");
        assertThrows(ArithmeticException.class, () -> calc.divide(10, 0),
                "Division by zero should throw ArithmeticException");
        assertEquals(-3, calc.divide(9, -3),
                "Dividing 9 by -3 should equal -3");
    }

    /**
     * Simple calculator class for testing purposes.
     */
    static class Calculator {

        public int add(int a, int b) { return a + b; }

        public int subtract(int a, int b) { return a - b; }

        public int divide(int a, int b) {
            if (b == 0) throw new ArithmeticException("Cannot divide by zero");
            return a / b;
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    // MANUAL GAME TESTS (Sprint 3)
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Manual game starts with the correct board state.
     * Centre hole is empty, game is not over, pegs are present.
     */
    @Test
    void manualGame_freshGame_correctInitialState() {
        ManualGame game = new ManualGame(7, BoardType.ENGLISH);

        assertFalse(game.isGameOver(),     "Game should not be over at the start");
        assertTrue(game.getPegCount() > 0, "Board should have pegs");
        assertEquals(HoleState.EMPTY, game.getBoard().getHole(3, 3),
                "Centre hole must be empty at start");
    }

    /**
     * A valid manual move removes the jumped peg and updates the peg count.
     */
    @Test
    void manualGame_validMove_removesJumpedPegAndUpdatesPegCount() {
        ManualGame game = new ManualGame(7, BoardType.ENGLISH);
        int pegsBefore = game.getPegCount();

        boolean moved = game.makeMove(3, 1, 3, 3);

        assertTrue(moved,              "Valid move should return true");
        assertEquals(pegsBefore - 1, game.getPegCount(), "Peg count should decrease by 1");
        assertEquals(HoleState.EMPTY, game.getBoard().getHole(3, 1), "Source must be empty");
        assertEquals(HoleState.EMPTY, game.getBoard().getHole(3, 2), "Jumped peg must be removed");
        assertEquals(HoleState.PEG,   game.getBoard().getHole(3, 3), "Destination must have peg");
    }

    /**
     * An invalid manual move is rejected and the board is unchanged.
     */
    @Test
    void manualGame_invalidMove_returnsFalseAndBoardUnchanged() {
        ManualGame game = new ManualGame(7, BoardType.ENGLISH);
        int pegsBefore = game.getPegCount();

        boolean moved = game.makeMove(3, 3, 3, 1); // centre is empty, invalid

        assertFalse(moved,             "Invalid move should return false");
        assertEquals(pegsBefore, game.getPegCount(), "Peg count must not change");
    }

    /**
     * Manual game ends automatically when no valid moves remain.
     */
    @Test
    void manualGame_gameOver_detectedAutomatically() {
        ManualGame game = new ManualGame(7, BoardType.ENGLISH);
        int safety = 0;
        while (game.getBoard().hasValidMoves() && safety++ < 2000)
            makeFirstMove(game.getBoard());

        assertFalse(game.getBoard().hasValidMoves(), "No moves should remain at game over");
    }

    /**
     * Performance rating is a valid string when manual game ends.
     */
    @Test
    void manualGame_gameOver_validPerformanceRating() {
        ManualGame game = new ManualGame(7, BoardType.ENGLISH);
        int safety = 0;
        while (game.getBoard().hasValidMoves() && safety++ < 2000)
            makeFirstMove(game.getBoard());

        String rating = game.getPerformanceRating();
        assertTrue(rating.equals("Outstanding") || rating.equals("Very Good")
                        || rating.equals("Good")        || rating.equals("Average"),
                "Rating must be one of the four valid values, got: " + rating);
    }

    /**
     * Randomize keeps the same peg count and leaves at least one valid move.
     */
    @Test
    void manualGame_randomize_preservesPegCountAndPlayability() {
        ManualGame game = new ManualGame(7, BoardType.ENGLISH);
        int before = game.getPegCount();

        game.randomize();

        assertEquals(before, game.getPegCount(), "Peg count must not change after randomize");
        assertTrue(game.getBoard().hasValidMoves(), "Board must remain playable after randomize");
    }

    // ════════════════════════════════════════════════════════════════════════════
    // AUTOMATED GAME TESTS (Sprint 3)
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Automated game starts with the correct board state.
     */
    @Test
    void automatedGame_freshGame_correctInitialState() {
        AutomatedGame game = new AutomatedGame(7, BoardType.ENGLISH);

        assertFalse(game.isGameOver(),     "Game should not be over at the start");
        assertTrue(game.getPegCount() > 0, "Board should have pegs");
        assertFalse(game.getAllValidMoves().isEmpty(), "Valid moves must exist on a fresh board");
    }

    /**
     * A single automated move reduces the peg count by exactly one.
     */
    @Test
    void automatedGame_singleAutoMove_reducesPegCountByOne() {
        AutomatedGame game = new AutomatedGame(7, BoardType.ENGLISH);
        int before = game.getPegCount();

        boolean moved = game.makeAutoMove();

        assertTrue(moved,              "Automated move should succeed on a fresh board");
        assertEquals(before - 1, game.getPegCount(), "Peg count must decrease by exactly 1");
    }

    /**
     * Every automated move selects only valid jumps — peg count decreases by 1 each time.
     */
    @Test
    void automatedGame_tenMoves_eachRemovesExactlyOnePeg() {
        AutomatedGame game = new AutomatedGame(7, BoardType.ENGLISH);

        for (int i = 0; i < 10 && game.getBoard().hasValidMoves(); i++) {
            int before = game.getPegCount();
            game.makeAutoMove();
            assertEquals(before - 1, game.getPegCount(),
                    "Move " + (i + 1) + " should remove exactly one peg");
        }
    }

    /**
     * Automated game ends when no valid moves remain.
     */
    @Test
    void automatedGame_gameOver_detectedWhenNoMovesRemain() {
        AutomatedGame game = new AutomatedGame(7, BoardType.ENGLISH);
        int safety = 0;
        while (game.getBoard().hasValidMoves() && safety++ < 2000)
            game.makeAutoMove();

        assertFalse(game.getBoard().hasValidMoves(), "No moves should remain at game over");
        assertFalse(game.makeAutoMove(), "makeAutoMove should return false when game is over");
    }

    /**
     * Performance rating is a valid string when automated game ends.
     */
    @Test
    void automatedGame_gameOver_validPerformanceRating() {
        AutomatedGame game = new AutomatedGame(7, BoardType.ENGLISH);
        int safety = 0;
        while (game.getBoard().hasValidMoves() && safety++ < 2000)
            game.makeAutoMove();

        String rating = game.getPerformanceRating();
        assertTrue(rating.equals("Outstanding") || rating.equals("Very Good")
                        || rating.equals("Good")        || rating.equals("Average"),
                "Rating must be one of the four valid values, got: " + rating);
    }

    /**
     * Automated game mode name is "Automated".
     */
    @Test
    void automatedGame_modeName_isAutomated() {
        AutomatedGame game = new AutomatedGame(7, BoardType.ENGLISH);
        assertEquals("Automated", game.getModeName());
    }

    // ════════════════════════════════════════════════════════════════════════════
    // Helper
    // ════════════════════════════════════════════════════════════════════════════

    private boolean makeFirstMove(Board board) {
        int[][] dirs = {{0,2},{0,-2},{2,0},{-2,0}};
        for (int r = 0; r < board.getSize(); r++)
            for (int c = 0; c < board.getSize(); c++)
                if (board.getHole(r, c) == HoleState.PEG)
                    for (int[] d : dirs)
                        if (board.tryMove(r, c, r+d[0], c+d[1])) return true;
        return false;
    }
}