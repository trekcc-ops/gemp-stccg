package com.gempukku.stccg.requirement.trigger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.missionattempt.AttemptMissionAction;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.PlayerResolver;
import com.gempukku.stccg.player.PlayerSource;

import java.util.Objects;

public class StartOfMissionAttemptTriggerChecker implements TriggerChecker {

    private final PlayerSource _attemptingPlayer;
    private boolean _anyPlayer;
    private final boolean _yourFirstAttemptOnly;
    private final FilterBlueprint _missionFilter;

    StartOfMissionAttemptTriggerChecker(@JsonProperty(value = "player", required = true)
            String playerText,
                 @JsonProperty(value = "yourFirstAttemptOfThisMissionOnly")
                         boolean yourFirstAttemptOnly,
                                        @JsonProperty(value = "mission")
                                        FilterBlueprint missionFilter
    ) throws InvalidCardDefinitionException {
        _missionFilter = missionFilter;
        _yourFirstAttemptOnly = yourFirstAttemptOnly;
        if (Objects.equals(playerText, "any")) {
            _attemptingPlayer = null;
            _anyPlayer = true;
        } else {
            _attemptingPlayer = PlayerResolver.resolvePlayer(playerText);
            _anyPlayer = false;
        }
        if (yourFirstAttemptOnly && !Objects.equals(playerText, "you")) {
            throw new InvalidCardDefinitionException("Cannot use the property 'yourFirstAttemptOfThisMissionOnly' on a " +
                    "mission attempt not restricted to 'you'");
        }
    }

    public boolean accepts(GameTextContext actionContext, DefaultGame cardGame) {
        ActionResult currentResult = cardGame.getCurrentActionResult();
        if (currentResult != null && currentResult.hasType(ActionResult.Type.START_OF_MISSION_ATTEMPT) &&
                cardGame.getCurrentAction() instanceof AttemptMissionAction missionAction
        ) {
            if (!_anyPlayer) {
                String attemptingPlayerName = _attemptingPlayer.getPlayerName(cardGame, actionContext);
                if (!missionAction.getPerformingPlayerId().equals(attemptingPlayerName)) {
                    return false;
                }
            }
            if (_yourFirstAttemptOnly && !missionAction.isFirstAttemptForPlayerOfThisMission(cardGame)) {
                return false;
            }
            if (_missionFilter != null &&
                    !_missionFilter.getFilterable(cardGame, actionContext).accepts(cardGame, missionAction.getMission())) {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

}