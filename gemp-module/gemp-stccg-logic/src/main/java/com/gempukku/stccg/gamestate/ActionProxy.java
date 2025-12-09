package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;

import java.util.LinkedList;
import java.util.List;

public interface ActionProxy {

    default List<TopLevelSelectableAction> getPhaseActions(DefaultGame cardGame, Player player) {
        return new LinkedList<>();
    }

    List<TopLevelSelectableAction> getOptionalAfterActions(DefaultGame cardGame, String playerId, ActionResult actionResult);

    List<TopLevelSelectableAction> getRequiredAfterTriggers(DefaultGame cardGame, ActionResult actionResult);

}