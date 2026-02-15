package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.game.DefaultGame;

import java.util.LinkedList;
import java.util.List;

public abstract class AbstractActionProxy implements ActionProxy {


    @Override
    public List<TopLevelSelectableAction> getOptionalAfterActions(DefaultGame cardGame, String playerId, ActionResult actionResult) {
        return new LinkedList<>();
    }

    @Override
    public List<TopLevelSelectableAction> getRequiredAfterTriggers(DefaultGame cardGame, ActionResult actionResult) {
        return new LinkedList<>();
    }

}