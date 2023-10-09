package com.gempukku.stccg.db;

import com.gempukku.stccg.game.User;

import java.util.Collection;

public interface LeagueParticipationDAO {
    void userJoinsLeague(String leagueId, User player, String remoteAddr);

    Collection<String> getUsersParticipating(String leagueId);
}
