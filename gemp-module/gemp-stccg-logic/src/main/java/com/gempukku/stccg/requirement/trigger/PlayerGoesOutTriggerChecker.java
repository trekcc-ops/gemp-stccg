package com.gempukku.stccg.requirement.trigger;

import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.player.PlayerSource;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.YouPlayerSource;

import java.util.Objects;

public class PlayerGoesOutTriggerChecker implements TriggerChecker {

    private final PlayerSource _playerSource = new YouPlayerSource();

    @Override
    public boolean accepts(ActionContext actionContext, DefaultGame cardGame) {
        ActionResult currentActionResult = cardGame.getCurrentActionResult();
        if (currentActionResult == null) {
            return false;
        } else {
            return playerGoesOut(cardGame.getCurrentActionResult(),
                    _playerSource.getPlayerName(cardGame, actionContext));
        }
    }

    private static boolean playerGoesOut(ActionResult actionResult, String playerId) {
        return (actionResult.hasType(ActionResult.Type.PLAYER_WENT_OUT) &&
                Objects.equals(actionResult.getPerformingPlayerId(), playerId));
    }
}