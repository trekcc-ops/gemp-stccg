package com.gempukku.stccg.actions.turn;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.PlayerNotFoundException;
import com.gempukku.stccg.modifiers.ModifiersLogic;

public class StartTurnAction extends SystemQueueAction {

    public StartTurnAction(DefaultGame cardGame) {
        super(cardGame);
        appendEffect(new AllowResponsesAction(cardGame, ActionResult.Type.START_OF_TURN));
    }
    @Override
    public Action nextAction(DefaultGame cardGame) throws PlayerNotFoundException {
        Action nextAction = getNextAction();
        if (nextAction != null)
            return nextAction;

        ModifiersLogic logic = cardGame.getGameState().getModifiersLogic();
        logic.signalStartOfTurn(cardGame.getCurrentPlayer());
        return getNextAction();
    }
}