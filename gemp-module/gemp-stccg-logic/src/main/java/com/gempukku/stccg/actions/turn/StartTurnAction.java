package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.ModifiersLogic;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

public class StartTurnAction extends SystemQueueAction {

    public StartTurnAction(DefaultGame cardGame, Player currentPlayer) {
        super(cardGame);
        saveResult(new ActionResult(ActionResult.Type.START_OF_TURN, currentPlayer.getPlayerId(), this));
    }
    @Override
    protected void processEffect(DefaultGame cardGame) throws PlayerNotFoundException {
        ModifiersLogic logic = cardGame.getGameState().getModifiersLogic();
        logic.signalStartOfTurn(cardGame, cardGame.getCurrentPlayer());
        setAsSuccessful();
    }
}