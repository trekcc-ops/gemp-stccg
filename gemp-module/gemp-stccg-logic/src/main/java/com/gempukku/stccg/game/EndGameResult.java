package com.gempukku.stccg.game;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collection;

public class EndGameResult {

    @JsonProperty("reason")
    private final EndGameResultType _type;

    @JsonProperty("winnerName")
    private final String _winnerName;

    @JsonProperty("loserNames")
    private final Collection<String> _loserNames;

    public EndGameResult(DefaultGame cardGame, EndGameResultType type) {
        _type = type;
        _winnerName = cardGame.getWinnerPlayerId();
        _loserNames = cardGame.getLosingPlayers();
    }
}