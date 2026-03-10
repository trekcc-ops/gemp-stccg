package com.gempukku.stccg.game;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.turn.EndGameActionType;

import java.util.Collection;

public class EndGameResult {

    @JsonProperty("reason")
    private final EndGameActionType _type;

    @JsonProperty("winnerName")
    private final String _winnerName;

    @JsonProperty("loserNames")
    private final Collection<String> _loserNames;

    public EndGameResult(DefaultGame cardGame, EndGameActionType type) {
        _type = type;
        _winnerName = cardGame.getWinnerPlayerId();
        _loserNames = cardGame.getLosingPlayers();
    }
}