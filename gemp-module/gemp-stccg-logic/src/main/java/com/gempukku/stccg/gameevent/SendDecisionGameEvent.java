package com.gempukku.stccg.gameevent;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.player.Player;

public class SendDecisionGameEvent extends GameEvent {

    private final GameState _gameState;
    private final String _decidingPlayerId;

    public SendDecisionGameEvent(DefaultGame cardGame, AwaitingDecision decision, Player decidingPlayer) {
        super(Type.DECISION, decidingPlayer);
        _decidingPlayerId = decidingPlayer.getPlayerId();
        _eventAttributes.put(Attribute.id, String.valueOf(decision.getDecisionId()));
        _eventAttributes.put(Attribute.decisionType, decision.getDecisionType().name());
        if (decision.getText() != null)
            _eventAttributes.put(Attribute.text, decision.getText());
        _eventAttributes.put(Attribute.phase, cardGame.getCurrentPhase().name());
        _gameState = cardGame.getGameState();
    }

    @JsonProperty("gameState")
    private String getGameState() throws JsonProcessingException {
        return _gameState.serializeForPlayer(_decidingPlayerId);
    }


}