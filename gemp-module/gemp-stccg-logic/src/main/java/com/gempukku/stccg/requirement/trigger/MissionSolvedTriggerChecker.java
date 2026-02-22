package com.gempukku.stccg.requirement.trigger;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.missionattempt.MissionSolvedActionResult;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.PlayerResolver;
import com.gempukku.stccg.player.PlayerSource;

public class MissionSolvedTriggerChecker implements TriggerChecker {

    private final boolean _missionSpecialistHelpRequired;
    private final PlayerSource _playerSource;

    @JsonCreator
    private MissionSolvedTriggerChecker(@JsonProperty("player") String playerText,
                            @JsonProperty("missionSpecialistUsedSkillToHelp") boolean missionSpecialistHelped)
            throws InvalidCardDefinitionException {
        _missionSpecialistHelpRequired = missionSpecialistHelped;
        _playerSource = PlayerResolver.resolvePlayer(playerText);
    }


    @Override
    public boolean accepts(GameTextContext context, DefaultGame cardGame) {
        ActionResult actionResult = cardGame.getCurrentActionResult();
        if (actionResult instanceof MissionSolvedActionResult missionResult) {
            if (_playerSource.isPlayer(missionResult.getPerformingPlayerId(), cardGame, context)) {
                if (missionResult.didMissionSpecialistHelp() || !_missionSpecialistHelpRequired) {
                    return true;
                }
            }
        }
        return false;
    }
}