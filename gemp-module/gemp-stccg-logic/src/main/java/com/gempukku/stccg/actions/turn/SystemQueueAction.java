package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.game.DefaultGame;

public abstract class SystemQueueAction extends ActionyAction {

    protected SystemQueueAction(DefaultGame game, String performingPlayerName) {
        super(game, performingPlayerName, ActionType.SYSTEM_QUEUE);
    }

    protected SystemQueueAction(DefaultGame game, ActionContext actionContext, String performingPlayerName) {
        super(game, performingPlayerName, ActionType.SYSTEM_QUEUE, actionContext);
    }

    public boolean requirementsAreMet(DefaultGame cardGame) { return true; }

}