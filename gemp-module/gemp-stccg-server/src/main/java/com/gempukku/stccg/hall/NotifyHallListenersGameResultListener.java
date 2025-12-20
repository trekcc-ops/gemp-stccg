package com.gempukku.stccg.hall;

import com.gempukku.stccg.game.GameResultListener;

import java.util.Map;

public class NotifyHallListenersGameResultListener implements GameResultListener {

    private final HallServer _hallServer;

    NotifyHallListenersGameResultListener(HallServer hallServer) {
        _hallServer = hallServer;
    }

    @Override
    public final void gameCancelled() {
        _hallServer.hallChanged();
    }

    @Override
    public final void gameFinished(String winnerPlayerId, String winReason,
                                   Map<String, String> loserReasons) {
        _hallServer.hallChanged();
    }
}