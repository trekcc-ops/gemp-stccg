package com.gempukku.stccg.hall;

import com.gempukku.stccg.tournament.Tournament;
import com.gempukku.stccg.tournament.TournamentQueueCallback;

import java.util.Map;

class HallTournamentQueueCallback implements TournamentQueueCallback {

    private final Map<? super String, ? super Tournament> _tournaments;

    HallTournamentQueueCallback(Map<? super String, ? super Tournament> tournaments) {
        _tournaments = tournaments;
    }

    @Override
    public final void createTournament(Tournament tournament) {
        String tournamentId = tournament.getTournamentId();
        _tournaments.put(tournamentId, tournament);
    }
}