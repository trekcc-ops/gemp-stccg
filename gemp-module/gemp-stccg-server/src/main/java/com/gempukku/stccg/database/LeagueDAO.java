package com.gempukku.stccg.database;

import com.gempukku.stccg.league.League;
import com.gempukku.stccg.league.LeagueSeriesData;
import com.gempukku.stccg.league.NewSealedLeagueData;

import java.sql.SQLException;
import java.util.List;

public interface LeagueDAO {
    void addLeague(int cost, String name, String type, String clazz, String parameters, int start, int endTime);

    void addLeague(List<LeagueSeriesData> seriesData, NewSealedLeagueData leagueData);

    List<League> loadActiveLeagues(int currentTime) throws SQLException;

    void setStatus(League league, int newStatus);
}