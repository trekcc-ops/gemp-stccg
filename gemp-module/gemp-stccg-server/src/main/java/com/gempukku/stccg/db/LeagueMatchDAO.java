package com.gempukku.stccg.db;

import com.gempukku.stccg.db.vo.LeagueMatchResult;

import java.util.Collection;

public interface LeagueMatchDAO {
    Collection<LeagueMatchResult> getLeagueMatches(String leagueId);

    void addPlayedMatch(String leagueId, String seriesId, String winner, String loser);
}