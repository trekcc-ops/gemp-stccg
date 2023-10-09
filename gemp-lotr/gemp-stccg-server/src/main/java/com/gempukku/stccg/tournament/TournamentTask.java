package com.gempukku.stccg.tournament;

import com.gempukku.stccg.collection.CollectionsManager;

public interface TournamentTask {
    void executeTask(TournamentCallback tournamentCallback, CollectionsManager collectionsManager);

    long getExecuteAfter();
}
