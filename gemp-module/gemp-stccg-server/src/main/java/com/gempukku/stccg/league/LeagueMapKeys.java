package com.gempukku.stccg.league;

public class LeagueMapKeys {
    public static String getLeagueMapKey(League league) {
        return league.getType();
    }

    public static String getLeagueSeriesMapKey(League league, LeagueSeries series) {
        return league.getType() + ":" + series.getName();
    }

}