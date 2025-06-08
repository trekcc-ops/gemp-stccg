package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.game.DefaultGame;

public class AllowResponsesAction extends SystemQueueAction {

    private final ActionResult _result;

    public AllowResponsesAction(DefaultGame game, ActionResult.Type type) {
        super(game);
        _result = new ActionResult(type);
    }

    public AllowResponsesAction(DefaultGame game, ActionResult result) {
        super(game);
        _result = result;
    }


    @Override
    protected void processEffect(DefaultGame cardGame) {
        cardGame.getActionsEnvironment().emitEffectResult(_result);
        setAsSuccessful();
    }

}