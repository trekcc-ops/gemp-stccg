package com.gempukku.stccg.gameevent;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.player.Player;

public class InitializeBoardForPlayerGameEvent extends GameEvent {

    private String _serializedGameState;

    public InitializeBoardForPlayerGameEvent(DefaultGame cardGame, Player player) {
        super(Type.PARTICIPANTS, player);
        try {
            _serializedGameState = cardGame.getGameState().serializeForPlayer(player.getPlayerId());
        } catch(JsonProcessingException exp) {

        }
    }

    @JsonProperty("gameState")
    private JsonNode getGameStateView() throws JsonProcessingException {
        return new ObjectMapper().readTree(_serializedGameState);
    }

}