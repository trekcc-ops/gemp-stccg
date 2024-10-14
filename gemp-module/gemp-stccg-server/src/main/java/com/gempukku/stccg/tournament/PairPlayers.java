package com.gempukku.stccg.tournament;

import com.gempukku.stccg.collection.CollectionsManager;

class PairPlayers implements TournamentTask {
    private final long executeAfter;
    private final DefaultTournament _tournament;

    PairPlayers(DefaultTournament tournament, long taskStart) {
        executeAfter = taskStart;
        _tournament = tournament;
    }

    @Override
    public final void executeTask(TournamentCallback tournamentCallback, CollectionsManager collectionsManager) {
        _tournament.doPairing(tournamentCallback, collectionsManager);
    }

    @Override
    public final long getExecuteAfter() {
        return executeAfter;
    }
}