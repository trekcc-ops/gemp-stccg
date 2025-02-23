package com.gempukku.stccg.gameevent;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.gempukku.stccg.gamestate.GameState;

public class ActionResultGameEvent extends GameEvent {

    private final GameState _gameState;
    private final String _playerId;

    public ActionResultGameEvent(GameState gameState, String playerId) {
        super(Type.ACTION_RESULT);
        _gameState = gameState;
        _playerId = playerId;
    }

    @JsonProperty("gameState")
    private String getGameState() throws JsonProcessingException {
        return _gameState.serializeForPlayer(_playerId);
    }

}