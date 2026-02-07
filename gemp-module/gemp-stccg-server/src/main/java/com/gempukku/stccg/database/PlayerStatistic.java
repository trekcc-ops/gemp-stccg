package com.gempukku.stccg.database;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PlayerStatistic {

    @JsonProperty("deckName")
    private final String _deckName;

    @JsonProperty("formatName")
    private final String _formatName;

    @JsonProperty("wins")
    private final int _wins;

    @JsonProperty("losses")
    private final int _losses;

    public PlayerStatistic(String deckName, String formatName, int wins, int losses) {
        _deckName = deckName;
        _formatName = formatName;
        _wins = wins;
        _losses = losses;
    }

}