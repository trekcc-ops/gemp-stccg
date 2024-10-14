package com.gempukku.stccg.tournament;

import com.gempukku.stccg.collection.CollectionsManager;

import java.util.Map;

class CreateMissingGames implements TournamentTask {
    private final Map<String, String> _gamesToCreate;
    private final DefaultTournament _tournament;

    public CreateMissingGames(DefaultTournament tournament, Map<String, String> gamesToCreate) {
        _gamesToCreate = gamesToCreate;
        _tournament = tournament;
    }

    @Override
    public void executeTask(TournamentCallback tournamentCallback, CollectionsManager collectionsManager) {
        for (Map.Entry<String, String> pairings : _gamesToCreate.entrySet()) {
            String playerOne = pairings.getKey();
            String playerTwo = pairings.getValue();
            _tournament.createNewGame(tournamentCallback, playerOne, playerTwo);
        }
    }

    @Override
    public long getExecuteAfter() {
        return 0;
    }
}