package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;

public class AllowResponsesAction extends SystemQueueAction {

    private final ActionResult _result;

    public AllowResponsesAction(DefaultGame game, ActionResult.Type type, Action action) {
        super(game);
        _result = new ActionResult(type, action);
    }

    public AllowResponsesAction(DefaultGame game, ActionResult.Type type, Player performingPlayer) {
        super(game);
        _result = new ActionResult(type, performingPlayer.getPlayerId());
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