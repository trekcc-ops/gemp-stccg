package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.game.DefaultGame;

import java.util.LinkedList;
import java.util.List;

public abstract class AbstractActionProxy implements ActionProxy {


    @Override
    public List<Action> getOptionalAfterActions(DefaultGame cardGame, String playerId, ActionResult actionResult) {
        return new LinkedList<>();
    }

    @Override
    public List<Action> getRequiredAfterTriggers(DefaultGame cardGame, ActionResult actionResult) {
        return new LinkedList<>();
    }

}