package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.PlayerNotFoundException;

public abstract class SystemQueueAction extends ActionyAction {

    protected SystemQueueAction(DefaultGame game, String performingPlayerName) {
        super(game, performingPlayerName, ActionType.SYSTEM_QUEUE);
    }


    protected SystemQueueAction(DefaultGame game, ActionContext actionContext, String performingPlayerName) {
        super(game, performingPlayerName, ActionType.SYSTEM_QUEUE, actionContext);
    }


    public boolean requirementsAreMet(DefaultGame cardGame) { return true; }

    @Override
    public Action nextAction(DefaultGame cardGame)
            throws InvalidGameLogicException, CardNotFoundException, PlayerNotFoundException {
        if (isCostFailed()) {
            setAsFailed();
            return null;
        } else {
            Action cost = getNextCost();
            if (cost != null)
                return cost;

            Action nextAction = getNextAction();
            if (nextAction != null)
                return nextAction;

            processEffect(cardGame);
            return null;
        }
    }

    protected void processEffect(DefaultGame cardGame) {
        setAsSuccessful();
    }
}