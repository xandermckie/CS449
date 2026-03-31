package com.solitaire;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Automated game mode — the computer selects and executes moves automatically.
 *
 * <p>Extends {@link Game} and overrides move behavior to pick a random valid
 * move each step rather than waiting for player input.
 *
 * <p>User Story 6: As a player, I want to watch an automated game where the
 * computer makes all the moves so that I can observe a full game play out.
 *
 * <p>User Story 7: As a player, I want the automated game to end automatically
 * when no valid moves remain so that I can see the final result.
 */
public class AutomatedGame extends Game {

    private static final Random RNG = new Random();

    /**
     * Creates a new automated game with the given board size and type.
     *
     * <p>User Story 3 / AC 3.2: Given the player has selected Automated mode,
     * When they click New Game, Then a new automated game is initialized and
     * the computer is ready to play moves.
     */
    public AutomatedGame(int size, BoardType type) {
        super(size, type);
    }

    /**
     * Selects a random valid move and executes it automatically.
     * The fromRow/fromCol/toRow/toCol parameters are ignored — the computer
     * chooses its own move.
     *
     * <p>AC 6.1: Given an automated game is in progress with valid moves remaining,
     * When makeMove is called, Then the computer selects a valid move at random
     * and executes it, removing the jumped peg.
     *
     * <p>AC 7.1: When no valid moves remain, returns false and game is over.
     *
     * @return true if a move was made; false if no moves remain
     */
    @Override
    public boolean makeMove(int fromRow, int fromCol, int toRow, int toCol) {
        List<int[]> moves = getAllValidMoves();
        if (moves.isEmpty()) return false;

        int[] chosen = moves.get(RNG.nextInt(moves.size()));
        return board.tryMove(chosen[0], chosen[1], chosen[2], chosen[3]);
    }

    /**
     * Convenience method — makes one automated move without needing coordinates.
     *
     * @return true if a move was made
     */
    public boolean makeAutoMove() {
        return makeMove(-1, -1, -1, -1);
    }

    /**
     * Returns all valid moves currently available on the board.
     * Useful for tests and for the autoplay step timer.
     */
    public List<int[]> getAllValidMoves() {
        List<int[]> moves = new ArrayList<>();
        int size = board.getSize();
        int[][] dirs = {{-2,0},{2,0},{0,-2},{0,2}};

        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                if (board.getHole(r, c) == HoleState.PEG)
                    for (int[] d : dirs)
                        if (board.isValidMove(r, c, r + d[0], c + d[1]))
                            moves.add(new int[]{r, c, r + d[0], c + d[1]});

        return moves;
    }

    @Override
    public String getModeName() { return "Automated"; }
}