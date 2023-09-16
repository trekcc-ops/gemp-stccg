package com.gempukku.lotro.db;

import com.gempukku.lotro.db.vo.LeagueMatchResult;

import java.util.Collection;

public interface LeagueMatchDAO {
    Collection<LeagueMatchResult> getLeagueMatches(String leagueId);

    void addPlayedMatch(String leagueId, String serieId, String winner, String loser);
}
