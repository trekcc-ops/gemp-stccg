package com.gempukku.stccg.database;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gempukku.stccg.league.League;

import java.sql.SQLException;
import java.util.List;

public interface LeagueDAO {
    void addLeague(League league);
    List<League> loadActiveLeagues() throws SQLException, JsonProcessingException;
    void setStatus(League league, int newStatus);
}