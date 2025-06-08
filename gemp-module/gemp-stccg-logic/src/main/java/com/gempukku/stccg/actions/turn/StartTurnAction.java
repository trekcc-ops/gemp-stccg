package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.modifiers.ModifiersLogic;

public class StartTurnAction extends SystemQueueAction {

    public StartTurnAction(DefaultGame cardGame, Player currentPlayer) {
        super(cardGame);
        appendEffect(new AllowResponsesAction(cardGame, ActionResult.Type.START_OF_TURN, currentPlayer));
    }
    @Override
    protected void processEffect(DefaultGame cardGame) throws PlayerNotFoundException {
        ModifiersLogic logic = cardGame.getGameState().getModifiersLogic();
        logic.signalStartOfTurn(cardGame.getCurrentPlayer());
        setAsSuccessful();
    }
}