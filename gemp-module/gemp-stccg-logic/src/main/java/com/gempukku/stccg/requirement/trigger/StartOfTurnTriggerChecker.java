package com.gempukku.stccg.requirement.trigger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.PlayerResolver;
import com.gempukku.stccg.player.PlayerSource;

import java.util.Objects;

public class StartOfTurnTriggerChecker implements TriggerChecker {

    private final PlayerSource _player;

    StartOfTurnTriggerChecker(@JsonProperty("turnPlayer") String playerText) throws InvalidCardDefinitionException {
        _player = (playerText == null) ? null : PlayerResolver.resolvePlayer(playerText);
    }
    public boolean accepts(GameTextContext actionContext, DefaultGame cardGame) {
        if (!cardGame.isCurrentActionResultType(ActionResult.Type.START_OF_TURN)) {
            return false;
        } else if (_player == null) {
            return true;
        } else {
            Action startTurnAction = cardGame.getCurrentAction();
            return startTurnAction != null &&
                    Objects.equals(startTurnAction.getPerformingPlayerId(), _player.getPlayerName(cardGame, actionContext));
        }
    }
}