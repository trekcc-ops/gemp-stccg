package com.gempukku.stccg.database;

import com.gempukku.stccg.league.League;

import java.sql.SQLException;
import java.util.List;

public interface LeagueDAO {
    void addLeague(League league);
    List<League> loadActiveLeagues() throws SQLException;
    void setStatus(League league);
}