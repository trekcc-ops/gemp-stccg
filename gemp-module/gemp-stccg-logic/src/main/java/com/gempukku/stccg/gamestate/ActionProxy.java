package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;

import java.util.LinkedList;
import java.util.List;

public interface ActionProxy<T extends DefaultGame> {

    default List<TopLevelSelectableAction> getPhaseActions(T cardGame, Player player) {
        return new LinkedList<>();
    }

    List<TopLevelSelectableAction> getOptionalAfterActions(T cardGame, String playerId, ActionResult actionResult);

    List<TopLevelSelectableAction> getRequiredAfterTriggers(T cardGame, ActionResult actionResult);

}