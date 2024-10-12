package com.gempukku.stccg.league;

public class LeagueMapKeys {
    public static String getLeagueMapKey(League league) {
        return league.getType();
    }

    public static String getLeagueSeriesMapKey(League league, LeagueSeriesData seriesData) {
        return league.getType() + ":" + seriesData.getName();
    }
}