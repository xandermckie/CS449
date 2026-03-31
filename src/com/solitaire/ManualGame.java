package com.solitaire;

/**
 * Manual game mode — the player specifies every move via the UI.
 *
 * <p>Extends {@link Game} and implements the player-driven move behavior.
 * All move validation is delegated to {@link Board#tryMove}.
 *
 * <p>User Story 4: As a player, I want to make a move in a manual game by
 * clicking a peg and then a valid destination so that I can play at my own pace.
 *
 * <p>User Story 5: As a player, I want the manual game to automatically end
 * when no valid moves remain so that I know my game is complete.
 */
public class ManualGame extends Game {

    /**
     * Creates a new manual game with the given board size and type.
     *
     * <p>User Story 3 / AC 3.1: Given the player has selected a board size, type,
     * and Manual mode, When they click New Game, Then a new manual game is started
     * with the board initialized to the standard starting position.
     */
    public ManualGame(int size, BoardType type) {
        super(size, type);
    }

    /**
     * Attempts to apply the player's chosen move.
     *
     * <p>AC 4.1: Given a valid move (fromRow, fromCol) → (toRow, toCol),
     * When the player clicks source then destination,
     * Then the move is executed, the jumped peg is removed, and the board updates.
     *
     * @return true if the move was valid and applied
     */
    @Override
    public boolean makeMove(int fromRow, int fromCol, int toRow, int toCol) {
        return board.tryMove(fromRow, fromCol, toRow, toCol);
    }

    @Override
    public String getModeName() { return "Manual"; }
}