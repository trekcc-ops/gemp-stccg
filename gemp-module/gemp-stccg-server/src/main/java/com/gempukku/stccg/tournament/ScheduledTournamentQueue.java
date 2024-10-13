package com.gempukku.stccg.tournament;

import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.collection.CollectionsManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

public class ScheduledTournamentQueue extends AbstractTournamentQueue {
    private static final long _signupTimeBeforeStart = 1000 * 60 * 60; // 60 minutes before start
    private final long _startTime;
    private final int _minimumPlayers;
    private final String _startCondition;
    private final String _tournamentName;
    private final Tournament.Stage _stage;
    private final String _scheduledTournamentId;

    public ScheduledTournamentQueue(TournamentQueueInfo tournamentQueueInfo, String tournamentId, long startTime,
                                    int minimumPlayers, String format, String tournamentName,
                                    Tournament.Stage stage, ServerObjects objects) {
        super(tournamentQueueInfo, objects);
        _scheduledTournamentId = tournamentId;
        _startTime = startTime;
        _minimumPlayers = minimumPlayers;
        _startCondition = format;
        _tournamentName = tournamentName;
        _stage = stage;
    }

    @Override
    public String getTournamentQueueName() {
        return _tournamentName;
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
    public synchronized boolean process(TournamentQueueCallback tournamentQueueCallback,
                                        CollectionsManager collectionsManager) throws SQLException, IOException {
        long now = System.currentTimeMillis();
        if (now > _startTime) {
            if (_players.size() >= _minimumPlayers) {

                for (String player : _players)
                    _tournamentService.addPlayer(_scheduledTournamentId, player, _playerDecks.get(player));

                Tournament tournament = _tournamentService.addTournament(_scheduledTournamentId, null, _tournamentName, _format, _collectionType, _stage,
                        _pairingMechanism.getRegistryRepresentation(), _tournamentPrizes.getRegistryRepresentation(), new Date());
                tournamentQueueCallback.createTournament(tournament);
            } else {
                _tournamentService.updateScheduledTournamentStarted(_scheduledTournamentId);
                leaveAllPlayers(collectionsManager);
            }

            return true;
        }
        return false;
    }

    @Override
    public boolean isJoinable() {
        return System.currentTimeMillis() >= _startTime - _signupTimeBeforeStart;
    }
}