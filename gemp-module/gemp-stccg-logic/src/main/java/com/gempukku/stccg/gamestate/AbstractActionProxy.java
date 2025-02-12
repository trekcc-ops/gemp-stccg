package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.player.Player;

import java.util.LinkedList;
import java.util.List;

public abstract class AbstractActionProxy implements ActionProxy {

    @Override
    public List<TopLevelSelectableAction> getPhaseActions(Player player) {
        return new LinkedList<>();
    }


    @Override
    public List<TopLevelSelectableAction> getOptionalAfterActions(String playerId, ActionResult actionResult) {
        return new LinkedList<>();
    }

    @Override
    public List<TopLevelSelectableAction> getRequiredAfterTriggers(ActionResult actionResult) {
        return new LinkedList<>();
    }

}