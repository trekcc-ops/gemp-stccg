package com.gempukku.stccg.competitive;

public class LeagueMatchResult implements CompetitiveMatchResult {
    private final String _winner;
    private final String _loser;
    private final String _seriesName;

    public LeagueMatchResult(String seriesName, String winner, String loser) {
        _seriesName = seriesName;
        _winner = winner;
        _loser = loser;
    }

    public String getSeriesName() {
        return _seriesName;
    }

    public String getLoser() {
        return _loser;
    }

    public String getWinner() {
        return _winner;
    }
}