package com.gempukku.lotro.tournament;

import java.util.List;
import java.util.Map;

public interface TournamentMatchDAO {
    void addMatch(String tournamentId, int round, String playerOne, String playerTwo);

    void setMatchResult(String tournamentId, int round, String winner);

    List<TournamentMatch> getMatches(String tournamentId);

    void addBye(String tournamentId, String player, int round);

    Map<String, Integer> getPlayerByes(String tournamentId);
}
