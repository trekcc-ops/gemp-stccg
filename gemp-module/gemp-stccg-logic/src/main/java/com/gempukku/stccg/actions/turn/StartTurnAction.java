package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;

public class StartTurnAction extends SystemQueueAction {

    public StartTurnAction(DefaultGame cardGame, String playerName) {
        super(cardGame, playerName);
    }
    @Override
    protected void processEffect(DefaultGame cardGame) {
        GameState gameState = cardGame.getGameState();
        gameState.signalStartOfTurn(cardGame, cardGame.getCurrentPlayerId());
        saveResult(new ActionResult(ActionResult.Type.START_OF_TURN, _performingPlayerId, this), cardGame);
        setAsSuccessful();
    }
}