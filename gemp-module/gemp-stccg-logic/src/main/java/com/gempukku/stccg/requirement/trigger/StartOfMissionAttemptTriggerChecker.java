package com.gempukku.stccg.requirement.trigger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.missionattempt.AttemptMissionAction;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.PlayerResolver;
import com.gempukku.stccg.player.PlayerSource;
import com.gempukku.stccg.player.YouPlayerSource;

import java.util.Objects;

public class StartOfMissionAttemptTriggerChecker implements TriggerChecker {

    private final PlayerSource _attemptingPlayer;
    private final boolean _yourFirstAttemptOnly;

    StartOfMissionAttemptTriggerChecker(@JsonProperty(value = "player", required = true)
            String playerText,
                 @JsonProperty(value = "yourFirstAttemptOfThisMissionOnly", required = true)
                         boolean yourFirstAttemptOnly
    ) throws InvalidCardDefinitionException {
        _yourFirstAttemptOnly = yourFirstAttemptOnly;
        _attemptingPlayer = (playerText == null) ? new YouPlayerSource() : PlayerResolver.resolvePlayer(playerText);
        if (yourFirstAttemptOnly && !Objects.equals(playerText, "you")) {
            throw new InvalidCardDefinitionException("Cannot use the property 'yourFirstAttemptOfThisMissionOnly' on a " +
                    "mission attempt not restricted to 'you'");
        }
    }

    public boolean accepts(GameTextContext actionContext, DefaultGame cardGame) {
        String attemptingPlayerName = _attemptingPlayer.getPlayerName(cardGame, actionContext);
        ActionResult currentResult = cardGame.getCurrentActionResult();
        if (currentResult != null && currentResult.hasType(ActionResult.Type.START_OF_MISSION_ATTEMPT) &&
                cardGame.getCurrentAction() instanceof AttemptMissionAction missionAction &&
                missionAction.getPerformingPlayerId().equals(attemptingPlayerName)
        ) {
            if (_yourFirstAttemptOnly) {
                return missionAction.isFirstAttemptForPlayerOfThisMission(cardGame);
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

}