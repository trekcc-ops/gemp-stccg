package com.gempukku.stccg.tournament;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.formats.GameFormat;

import java.io.IOException;
import java.sql.SQLException;

public class ScheduledTournamentQueue extends TournamentQueue {
    private static final long _signupTimeBeforeStart = 1000 * 60 * 60; // 60 minutes before start
    private final long _startTime;
    private final int _minimumPlayers;
    private final String _startCondition;
    private final String _scheduledTournamentId;
    private boolean _shouldBeRemoved;

    public ScheduledTournamentQueue(TournamentQueueInfo queueInfo, String tournamentId, long startTime,
                                    int minimumPlayers, String startCondition, String tournamentName,
                                    Tournament.Stage stage, CardBlueprintLibrary cardBlueprintLibrary,
                                    TournamentService tournamentService, GameFormat gameFormat) {
        super(queueInfo, queueInfo.getPrizes(cardBlueprintLibrary), tournamentService, stage, tournamentName,
                gameFormat);
        _scheduledTournamentId = tournamentId;
        _startTime = startTime;
        _minimumPlayers = minimumPlayers;
        _startCondition = startCondition;
    }


    @Override
    public String getPairingDescription() {
        return _pairingMechanism.getPlayOffSystem() + ", minimum players: " + _minimumPlayers;
    }

    @Override
    public String getStartCondition() {
        return _startCondition;
    }

    @Override
    public synchronized void process(TournamentQueueCallback tournamentQueueCallback,
                                     CollectionsManager collectionsManager, TournamentService tournamentService)
            throws SQLException, IOException {
        if (isWaitingForStartTime()) {
            _shouldBeRemoved = false;
        } else if (hasEnoughPlayers()) {
            Tournament tournament = tournamentService.addTournament(this, _scheduledTournamentId, _queueName);
            tournamentQueueCallback.createTournament(tournament);
            addPlayersToTournamentData(tournamentService, _scheduledTournamentId);
            _shouldBeRemoved = true;
        } else {
            tournamentService.updateScheduledTournamentStarted(_scheduledTournamentId);
            leaveAllPlayers(collectionsManager);
            _shouldBeRemoved = true;
        }
    }

    private boolean isWaitingForStartTime() {
        long now = System.currentTimeMillis();
        return now < _startTime;
    }

    private boolean hasEnoughPlayers() {
        return _players.size() >= _minimumPlayers;
    }

    private void addPlayersToTournamentData(TournamentService tournamentService, String tournamentId) {
        _players.forEach(player -> tournamentService.addPlayer(tournamentId, player,
                _playerDecks.get(player)));
    }

    @Override
    public boolean isJoinable() {
        return System.currentTimeMillis() >= _startTime - _signupTimeBeforeStart;
    }

    @Override
    public boolean shouldBeRemovedFromHall() {
        return _shouldBeRemoved;
    }
}