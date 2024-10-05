package com.gempukku.stccg.league;

import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.competitive.PlayerStanding;
import com.gempukku.stccg.db.User;

import java.util.List;

public interface LeagueData {
    boolean isSoloDraftLeague();

    List<LeagueSeriesData> getSeries();

    void joinLeague(CollectionsManager collectionsManager, User player, int currentTime);

    int process(CollectionsManager collectionsManager, List<PlayerStanding> leagueStandings, int oldStatus, int currentTime);

    default int getMaxRepeatMatchesPerSerie() {
        return 1;
    }
}
