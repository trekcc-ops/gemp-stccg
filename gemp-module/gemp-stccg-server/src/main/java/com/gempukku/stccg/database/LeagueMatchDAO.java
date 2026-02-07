package com.gempukku.stccg.database;

import com.gempukku.stccg.competitive.LeagueMatchResult;

import java.util.Collection;

public interface LeagueMatchDAO {
    Collection<LeagueMatchResult> getLeagueMatches(int leagueId);

    void addPlayedMatch(int leagueId, String seriesId, String winner, String loser);

}