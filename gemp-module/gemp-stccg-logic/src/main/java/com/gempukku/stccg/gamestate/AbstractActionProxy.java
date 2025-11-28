package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.game.DefaultGame;

import java.util.LinkedList;
import java.util.List;

public abstract class AbstractActionProxy<T extends DefaultGame> implements ActionProxy<T> {


    @Override
    public List<TopLevelSelectableAction> getOptionalAfterActions(T cardGame, String playerId, ActionResult actionResult) {
        return new LinkedList<>();
    }

    @Override
    public List<TopLevelSelectableAction> getRequiredAfterTriggers(T cardGame, ActionResult actionResult) {
        return new LinkedList<>();
    }

}