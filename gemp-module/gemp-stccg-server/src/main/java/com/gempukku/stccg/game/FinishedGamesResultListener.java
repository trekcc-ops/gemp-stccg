package com.gempukku.stccg.game;

import java.util.Date;
import java.util.Map;

public class FinishedGamesResultListener implements GameResultListener {

    private final GameServer _gameServer;
    private final String _gameId;

    public FinishedGamesResultListener(GameServer gameServer, String gameId) {
        _gameServer = gameServer;
        _gameId = gameId;
    }
    @Override
    public void gameFinished(String winnerPlayerId, String winReason, Map<String, String> loserReasons) {
        _gameServer.logGameEndTime(_gameId);
    }

    @Override
    public void gameCancelled() {
        _gameServer.logGameEndTime(_gameId);
    }
}