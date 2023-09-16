package com.gempukku.lotro.tournament;

import com.gempukku.lotro.collection.CollectionsManager;

public interface TournamentTask {
    void executeTask(TournamentCallback tournamentCallback, CollectionsManager collectionsManager);

    long getExecuteAfter();
}
