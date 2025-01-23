package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.game.Player;

import java.util.List;

public interface ActionProxy {

    List<TopLevelSelectableAction> getPhaseActions(Player player);

    List<TopLevelSelectableAction> getOptionalAfterActions(String playerId, ActionResult actionResult);

    List<TopLevelSelectableAction> getRequiredAfterTriggers(ActionResult actionResult);

}