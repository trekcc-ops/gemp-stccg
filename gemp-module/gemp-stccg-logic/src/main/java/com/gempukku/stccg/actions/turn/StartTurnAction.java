package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;

public class StartTurnAction extends ActionyAction {

    public StartTurnAction(DefaultGame cardGame, String playerName) {
        super(cardGame, playerName, ActionType.START_TURN);
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    protected void processEffect(DefaultGame cardGame) {
        GameState gameState = cardGame.getGameState();
        gameState.signalStartOfTurn(cardGame, cardGame.getCurrentPlayerId());
        saveResult(new ActionResult(cardGame, ActionResult.Type.START_OF_TURN, _performingPlayerId, this), cardGame);
        setAsSuccessful();
    }
}