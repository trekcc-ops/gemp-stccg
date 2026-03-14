package com.gempukku.stccg.requirement.trigger;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.missionattempt.MissionAttemptEndedResult;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.PlayerResolver;
import com.gempukku.stccg.player.PlayerSource;

public class MissionSolvedTriggerChecker implements TriggerChecker {

    private final boolean _missionSpecialistHelpRequired;
    private final PlayerSource _playerSource;
    private final FilterBlueprint _missionFilter;

    @JsonCreator
    private MissionSolvedTriggerChecker(@JsonProperty("player") String playerText,
                                        @JsonProperty("missionSpecialistUsedSkillToHelp") boolean missionSpecialistHelped,
                                        @JsonProperty("missionFilter")FilterBlueprint missionFilter)
            throws InvalidCardDefinitionException {
        _missionSpecialistHelpRequired = missionSpecialistHelped;
        _playerSource = PlayerResolver.resolvePlayer(playerText);
        _missionFilter = missionFilter;
    }


    @Override
    public boolean accepts(GameTextContext context, DefaultGame cardGame) {
        ActionResult actionResult = cardGame.getCurrentActionResult();
        if (actionResult instanceof MissionAttemptEndedResult missionResult && missionResult.wasSuccessful()) {
            if (_missionFilter != null && !_missionFilter.getFilterable(cardGame, context).accepts(cardGame, missionResult.mission())) {
                return false;
            } else if (_playerSource.isPlayer(missionResult.getPerformingPlayerId(), cardGame, context)) {
                if (missionResult.didMissionSpecialistHelp() || !_missionSpecialistHelpRequired) {
                    return true;
                }
            }
        }
        return false;
    }
}