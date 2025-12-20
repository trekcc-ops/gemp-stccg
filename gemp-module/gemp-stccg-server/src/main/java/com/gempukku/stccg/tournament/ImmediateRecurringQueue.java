package com.gempukku.stccg.tournament;

import com.gempukku.stccg.collection.CollectionType;
import com.gempukku.stccg.collection.CollectionsManager;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ImmediateRecurringQueue extends TournamentQueue {
    private final int _playerCap;
    private final String _tournamentIdPrefix;

    public ImmediateRecurringQueue(int cost, String format, CollectionType collectionType, String tournamentIdPrefix,
                                           String tournamentQueueName, int playerCap, boolean requiresDeck,
                                           TournamentService tournamentService, TournamentPrizes tournamentPrizes,
                                   PairingMechanism pairingMechanism) {
        super(cost, requiresDeck, collectionType, tournamentPrizes, pairingMechanism, format, tournamentService,
                Tournament.Stage.PLAYING_GAMES, tournamentQueueName);
        _playerCap = playerCap;
        _tournamentIdPrefix = tournamentIdPrefix;
    }

    @Override
    public String getStartCondition() {
        return "When "+_playerCap+" players join";
    }

    @Override
    public synchronized void process(TournamentQueueCallback tournamentQueueCallback,
                                     CollectionsManager collectionsManager, TournamentService tournamentService) {
        if (hasEnoughPlayers()) {
            String tournamentId = _tournamentIdPrefix + System.currentTimeMillis();
            ZonedDateTime now = ZonedDateTime.now();
            String currentDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String tournamentName = _queueName + " - " + currentDate;
            Tournament tournament = tournamentService.addTournament(this, tournamentId, tournamentName);
            addPlayersToTournamentData(tournamentService, tournamentId);
            tournamentQueueCallback.createTournament(tournament);
        }
    }

    private boolean hasEnoughPlayers() {
        return _players.size() >= _playerCap;
    }

    private void addPlayersToTournamentData(TournamentService tournamentService, String tournamentId) {
        for (int i=0; i<_playerCap; i++) {
            String player = _players.poll();
            tournamentService.addPlayer(tournamentId, player, _playerDecks.get(player));
            _playerDecks.remove(player);
        }
    }

    @Override
    public boolean isJoinable() {
        return true;
    }

    @Override
    public boolean shouldBeRemovedFromHall() {
        return false;
    }
}