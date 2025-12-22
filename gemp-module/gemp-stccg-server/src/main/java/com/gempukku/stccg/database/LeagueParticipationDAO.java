package com.gempukku.stccg.database;

import java.util.Collection;

public interface LeagueParticipationDAO {
    void userJoinsLeague(int leagueId, User player, String remoteAddress);

    Collection<String> getUsersParticipating(int leagueId);

}