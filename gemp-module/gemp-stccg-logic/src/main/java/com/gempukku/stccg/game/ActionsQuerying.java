package com.gempukku.stccg.game;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.gamestate.ActionProxy;

import java.util.Collection;

public interface ActionsQuerying {

    Action getCurrentAction();

    default ActionResult getCurrentActionResult() {
        Action currentAction = getCurrentAction();
        return (currentAction == null) ? null : currentAction.getResult();
    }

    default boolean isCurrentActionResultType(ActionResult.Type type) {
        return getCurrentActionResult() != null && getCurrentActionResult().getType() == type;
    }

    Collection<ActionProxy> getAllActionProxies();

}