package com.gempukku.stccg.game;

import java.util.Map;

public class GameMediatorListener implements GameResultListener {

    private final CardGameMediator _mediator;

    public GameMediatorListener(CardGameMediator mediator) {
        _mediator = mediator;
    }

    @Override
    public void gameFinished(String winnerPlayerId, String winReason, Map<String, String> loserReasons) {
        _mediator.gameFinished(winnerPlayerId, winReason, loserReasons);
        _mediator.logEndTime();
    }

    @Override
    public void gameCancelled() {
        _mediator.logEndTime();
    }
}