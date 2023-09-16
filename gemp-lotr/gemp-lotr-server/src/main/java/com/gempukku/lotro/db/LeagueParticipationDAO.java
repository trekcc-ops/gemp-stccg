package com.gempukku.lotro.db;

import com.gempukku.lotro.game.User;

import java.util.Collection;

public interface LeagueParticipationDAO {
    void userJoinsLeague(String leagueId, User player, String remoteAddr);

    Collection<String> getUsersParticipating(String leagueId);
}
