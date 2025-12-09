package com.gempukku.stccg.game;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.gamestate.ActionProxy;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public interface ActionsQuerying {

    default <T extends Action> List<T> getPlayableActions(String playerName, Iterable<T> actions) {
        List<T> result = new LinkedList<>();
        if (actions != null) {
            for (T action : actions) {
                if (getGame().playerRestrictedFromPerformingActionDueToModifiers(playerName, action) && action.canBeInitiated(getGame())) {
                    result.add(action);
                }
            }
        }
        return result;
    }


    DefaultGame getGame();

    Collection<ActionProxy> getAllActionProxies();

}