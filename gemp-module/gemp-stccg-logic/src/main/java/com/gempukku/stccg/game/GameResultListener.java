package com.gempukku.stccg.game;

import java.util.Map;

public interface GameResultListener {
    void gameFinished(String winnerPlayerId, String winReason, Map<String, String> loserPlayerIdsWithReasons);

    void gameCancelled();
}
