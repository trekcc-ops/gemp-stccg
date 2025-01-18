package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.TopLevelSelectableAction;

import java.util.List;

public abstract class AbstractActionProxy implements ActionProxy {
    @Override
    public List<TopLevelSelectableAction> getPhaseActions(String playerId) {
        return null;
    }

    @Override
    public List<TopLevelSelectableAction> getOptionalAfterActions(String playerId, ActionResult actionResult) {
        return null;
    }

    @Override
    public List<TopLevelSelectableAction> getRequiredAfterTriggers(ActionResult actionResult) {
        return null;
    }

}