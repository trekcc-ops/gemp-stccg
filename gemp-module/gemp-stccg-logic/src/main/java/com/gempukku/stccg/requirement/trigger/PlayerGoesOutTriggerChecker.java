package com.gempukku.stccg.requirement.trigger;

import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.PlayerSource;

import java.util.Objects;

public class PlayerGoesOutTriggerChecker implements TriggerChecker {

    private final PlayerSource _playerSource = ActionContext::getPerformingPlayerId;

    @Override
    public boolean accepts(ActionContext actionContext) {
        return playerGoesOut(actionContext.getEffectResult(),
                _playerSource.getPlayerId(actionContext));
    }

    private static boolean playerGoesOut(ActionResult actionResult, String playerId) {
        return (actionResult.getType() == ActionResult.Type.PLAYER_WENT_OUT &&
                Objects.equals(actionResult.getPerformingPlayerId(), playerId));
    }
}