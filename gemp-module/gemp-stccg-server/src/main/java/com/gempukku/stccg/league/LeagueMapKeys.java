package com.gempukku.stccg.league;

import com.gempukku.stccg.db.vo.League;

public class LeagueMapKeys {
    public static String getLeagueMapKey(League league) {
        return league.getType();
    }

    public static String getLeagueSeriesMapKey(League league, LeagueSeriesData leagueSerie) {
        return league.getType() + ":" + leagueSerie.getName();
    }
}