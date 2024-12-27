package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;

import java.util.List;

public interface ActionProxy {
    List<? extends Action> getPhaseActions(String playerId);

    List<? extends Action> getOptionalAfterActions(String playerId, ActionResult actionResult);

    List<? extends Action> getRequiredAfterTriggers(ActionResult actionResult);

}