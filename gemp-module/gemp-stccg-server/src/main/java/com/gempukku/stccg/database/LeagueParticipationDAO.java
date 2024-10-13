package com.gempukku.stccg.database;

import java.util.Collection;

public interface LeagueParticipationDAO {
    void userJoinsLeague(String leagueId, User player, String remoteAddress);

    Collection<String> getUsersParticipating(String leagueId);
}