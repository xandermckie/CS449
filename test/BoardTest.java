package com.solitaire;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Sprint 5 additional tests targeting coverage gaps identified in the code review.
 *
 * Focus areas:
 *   - GameRecord file I/O (previously ~70% coverage)
 *   - GameReplayer snapshot restore
 *   - AutomatedGame.getLastMove()
 *   - Board edge cases (out-of-bounds setHole, pegCount guard in randomizeHoles)
 */
class BoardTest_Sprint5 {

    // ── GameRecord file I/O ───────────────────────────────────────────────────────

    /**
     * A recording saved to a temp file and loaded back produces identical moves.
     * Covers: GameRecord.saveToFile(), GameRecord.loadFromFile()
     */
    @Test
    void recordAndReload_movesArePreserved(@TempDir Path tempDir) throws IOException {
        GameRecord record = new GameRecord(7, BoardType.ENGLISH, "Manual");
        record.recordMove(3, 1, 3, 3);
        record.recordMove(1, 3, 3, 3);
        record.recordEnd(30, "Average");

        File file = tempDir.resolve("test_recording.txt").toFile();
        record.saveToFile(file.getAbsolutePath());

        GameRecord loaded = GameRecord.loadFromFile(file.getAbsolutePath());

        assertEquals(7,                    loaded.getBoardSize());
        assertEquals(BoardType.ENGLISH,    loaded.getBoardType());
        assertEquals("Manual",             loaded.getModeName());
        assertEquals(3,                    loaded.getActions().size());

        GameRecord.RecordedAction move1 = loaded.getActions().get(0);
        assertEquals(GameRecord.RecordedAction.Type.MOVE, move1.type);
        assertEquals(3, move1.fromRow); assertEquals(1, move1.fromCol);
        assertEquals(3, move1.toRow);   assertEquals(3, move1.toCol);

        GameRecord.RecordedAction end = loaded.getActions().get(2);
        assertEquals(GameRecord.RecordedAction.Type.END, end.type);
        assertEquals(30,        end.finalPegCount);
        assertEquals("Average", end.rating);
    }

    /**
     * A RANDOMIZE action is saved with a board snapshot and restored exactly on reload.
     * Covers: GameRecord.recordRandomize(Board), snapshot round-trip
     */
    @Test
    void recordAndReload_randomizeSnapshotPreserved(@TempDir Path tempDir) throws IOException {
        Board board = new Board(7, BoardType.ENGLISH);
        board.tryMove(3, 1, 3, 3); // make a move first so board is non-standard

        GameRecord record = new GameRecord(7, BoardType.ENGLISH, "Manual");
        record.recordRandomize(board); // snapshot current state

        File file = tempDir.resolve("randomize_test.txt").toFile();
        record.saveToFile(file.getAbsolutePath());

        GameRecord loaded = GameRecord.loadFromFile(file.getAbsolutePath());
        assertEquals(1, loaded.getActions().size());

        GameRecord.RecordedAction action = loaded.getActions().get(0);
        assertEquals(GameRecord.RecordedAction.Type.RANDOMIZE, action.type);
        assertNotNull(action.boardSnapshot, "Board snapshot must not be null");
        assertFalse(action.boardSnapshot.isEmpty(), "Board snapshot must not be empty");
    }

    /**
     * GameReplayer restores a RANDOMIZE action to the exact same board state.
     * Covers: GameReplayer.restoreBoardSnapshot()
     */
    @Test
    void replayRestoresBoardSnapshotExactly(@TempDir Path tempDir) throws IOException {
        // Play one move then record the board state
        Board originalBoard = new Board(7, BoardType.ENGLISH);
        originalBoard.tryMove(3, 1, 3, 3);

        GameRecord record = new GameRecord(7, BoardType.ENGLISH, "Manual");
        record.recordRandomize(originalBoard);
        record.recordEnd(originalBoard.getPegCount(), originalBoard.getPerformanceRating());

        File file = tempDir.resolve("replay_snapshot.txt").toFile();
        record.saveToFile(file.getAbsolutePath());

        // Replay and verify the board matches the snapshot
        GameRecord loaded   = GameRecord.loadFromFile(file.getAbsolutePath());
        GameReplayer replayer = new GameReplayer(loaded);
        replayer.stepForward(); // apply RANDOMIZE

        Board replayedBoard = replayer.getBoard();
        int size = originalBoard.getSize();

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                assertEquals(originalBoard.getHole(r, c), replayedBoard.getHole(r, c),
                        "Hole (" + r + "," + c + ") must match after snapshot restore");
            }
        }
    }

    /**
     * Loading a file that has no MODE line throws IOException.
     * Covers: GameRecord.loadFromFile() error path
     */
    @Test
    void loadFromFile_missingModeLine_throwsIOException(@TempDir Path tempDir) throws IOException {
        File file = tempDir.resolve("bad_recording.txt").toFile();
        java.nio.file.Files.writeString(file.toPath(),
                "# Bad file\nSIZE:7\nTYPE:ENGLISH\nMOVE:3,1,3,3\n");

        assertThrows(IOException.class,
                () -> GameRecord.loadFromFile(file.getAbsolutePath()),
                "Missing MODE line should throw IOException");
    }

    // ── AutomatedGame.getLastMove() ───────────────────────────────────────────────

    /**
     * getLastMove() returns a non-null 4-element array after a successful move.
     * Covers: AutomatedGame.getLastMove()
     */
    @Test
    void automatedGame_getLastMove_notNullAfterMove() {
        AutomatedGame game = new AutomatedGame(7, BoardType.ENGLISH);
        assertNull(game.getLastMove(), "getLastMove should be null before any move");

        boolean moved = game.makeAutoMove();
        assertTrue(moved, "Move should succeed on a fresh board");

        int[] last = game.getLastMove();
        assertNotNull(last, "getLastMove must not be null after a successful move");
        assertEquals(4, last.length, "getLastMove must return 4 coordinates");
    }

    /**
     * getLastMove() returns null when no moves are available.
     */
    @Test
    void automatedGame_getLastMove_nullWhenNoMovesRemain() {
        AutomatedGame game = new AutomatedGame(7, BoardType.ENGLISH);
        int safety = 0;
        while (game.getBoard().hasValidMoves() && safety++ < 2000)
            game.makeAutoMove();

        boolean moved = game.makeAutoMove();
        assertFalse(moved, "makeAutoMove should return false when no moves remain");
        assertNull(game.getLastMove(), "getLastMove should be null when no moves available");
    }

    // ── Board edge cases ──────────────────────────────────────────────────────────

    /**
     * setHole() silently ignores out-of-bounds coordinates.
     * Covers: setHole() guard condition
     */
    @Test
    void board_setHole_outOfBounds_noException() {
        Board board = new Board(7, BoardType.ENGLISH);
        // These should not throw — just be silently ignored
        assertDoesNotThrow(() -> board.setHole(-1, 0, HoleState.PEG));
        assertDoesNotThrow(() -> board.setHole(0, -1, HoleState.PEG));
        assertDoesNotThrow(() -> board.setHole(99, 99, HoleState.PEG));
        assertDoesNotThrow(() -> board.setHole(7, 7, HoleState.PEG));
    }

    /**
     * setHole() does not modify INVALID holes.
     */
    @Test
    void board_setHole_invalidHole_notModified() {
        Board board = new Board(7, BoardType.ENGLISH);
        // Corner (0,0) is INVALID on English board
        assertEquals(HoleState.INVALID, board.getHole(0, 0));
        board.setHole(0, 0, HoleState.PEG);
        assertEquals(HoleState.INVALID, board.getHole(0, 0),
                "setHole must not modify INVALID holes");
    }

    /**
     * randomizeHoles() does not throw when called on a board with no pegs.
     * Covers: the pegCount == 0 guard added in Sprint 5.
     */
    @Test
    void board_randomizeHoles_noPegs_doesNotThrow() {
        Board board = new Board(3, BoardType.DIAMOND);
        int size = board.getSize();
        // Remove all pegs manually
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                if (board.getHole(r, c) == HoleState.PEG)
                    board.setHole(r, c, HoleState.EMPTY);

        assertEquals(0, board.getPegCount());
        assertDoesNotThrow(board::randomizeHoles,
                "randomizeHoles must not throw when no pegs remain");
    }

    // ── Recording with automated game ─────────────────────────────────────────────

    /**
     * Recording an automated game produces one MOVE entry per makeAutoMove() call.
     */
    @Test
    void automatedGame_recording_capturesEachMove(@TempDir Path tempDir) throws IOException {
        AutomatedGame game   = new AutomatedGame(7, BoardType.ENGLISH);
        GameRecord    record = new GameRecord(7, BoardType.ENGLISH, "Automated");

        int movesMade = 0;
        for (int i = 0; i < 5 && game.getBoard().hasValidMoves(); i++) {
            int before = game.getPegCount();
            boolean moved = game.makeAutoMove();
            if (moved) {
                int[] last = game.getLastMove();
                assertNotNull(last, "getLastMove must not be null after move");
                record.recordMove(last[0], last[1], last[2], last[3]);
                movesMade++;
            }
        }
        record.recordEnd(game.getPegCount(), game.getPerformanceRating());

        File file = tempDir.resolve("auto_record.txt").toFile();
        record.saveToFile(file.getAbsolutePath());

        GameRecord loaded = GameRecord.loadFromFile(file.getAbsolutePath());
        // Should have movesMade MOVE actions + 1 END
        assertEquals(movesMade + 1, loaded.getActions().size(),
                "Recorded action count should equal moves made + 1 END");
        for (int i = 0; i < movesMade; i++)
            assertEquals(GameRecord.RecordedAction.Type.MOVE, loaded.getActions().get(i).type);
    }
}