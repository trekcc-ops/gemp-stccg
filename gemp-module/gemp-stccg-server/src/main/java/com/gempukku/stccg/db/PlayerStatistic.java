package com.gempukku.stccg.db;

public class PlayerStatistic {
    private final String _deckName;
    private final String _formatName;
    private final int _wins;
    private final int _losses;

    public PlayerStatistic(String deckName, String formatName, int wins, int losses) {
        _deckName = deckName;
        _formatName = formatName;
        _wins = wins;
        _losses = losses;
    }

    public final String getDeckName() {
        return _deckName;
    }

    public final String getFormatName() {
        return _formatName;
    }

    public final int getWins() {
        return _wins;
    }

    public final int getLosses() {
        return _losses;
    }
}