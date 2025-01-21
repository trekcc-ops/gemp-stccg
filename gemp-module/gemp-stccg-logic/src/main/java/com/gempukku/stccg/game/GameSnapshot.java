package com.gempukku.stccg.game;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.gamestate.GameState;

/**
 * Defines a snapshot of a game. Since the DefaultGame class is not a snapshotable,
 * this class is used as a starting point to snapshot of all the elements of the game.
 */
public class GameSnapshot {
    private final int _id;
    private final String _description;
    private final JsonNode _gameState;


    public GameSnapshot(int id, String description, GameState gameState) {
        _id = id;
        _description = description;
        _gameState = new ObjectMapper().valueToTree(gameState);
    }


    /**
     * Gets the snapshot ID.
     * @return the snapshot ID
     */
    public int getId() {
        return _id;
    }

    /**
     * Gets the description.
     * @return the description
     */
    public String getDescription() {
        return _description;
    }

    /**
     * Gets the current player at time of snapshot.
     * @return the current player at time of snapshot
     */
    public String getCurrentPlayerId() {
        return _gameState.get("playerOrder").get("currentPlayer").textValue();
    }

    /**
     * Gets the turn number at time of snapshot.
     * @return the turn number at time of snapshot
     */
    public int getCurrentTurnNumber() {
        for (JsonNode playerNode : _gameState.get("players")) {
            if (playerNode.get("playerId").textValue().equals(getCurrentPlayerId())) {
                return playerNode.get("turnNumber").intValue();
            }
        }
        return 0;
    }

    /**
     * Gets the phase at time of snapshot.
     * @return the phase at time of snapshot
     */
    public Phase getCurrentPhase() {
        return Phase.valueOf(_gameState.get("currentPhase").textValue());
    }

}