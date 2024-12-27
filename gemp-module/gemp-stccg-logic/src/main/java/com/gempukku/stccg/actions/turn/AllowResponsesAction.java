package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.game.DefaultGame;

public class AllowResponsesAction extends SystemQueueAction {

    private final ActionResult.Type _type;

    public AllowResponsesAction(DefaultGame game, ActionResult.Type type) {
        super(game);
        _type = type;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) {
        cardGame.getActionsEnvironment().emitEffectResult(new ActionResult(_type));
        return getNextAction();
    }
}