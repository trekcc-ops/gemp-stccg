package com.gempukku.stccg.league;

class LeagueMapKeys {
    public static String getLeagueMapKey(League league) {
        return league.getType();
    }

    public static String getLeagueSeriesMapKey(League league, LeagueSeriesData seriesData) {
        return league.getType() + ":" + seriesData.getName();
    }
}