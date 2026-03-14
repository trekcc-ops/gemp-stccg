package com.gempukku.stccg.actions.missionattempt;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.ActionResultType;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.game.DefaultGame;

public class MissionAttemptStartedResult extends ActionResult {

    @JsonProperty("targetCardId")
    @JsonIdentityReference(alwaysAsId=true)
    private final MissionCard _mission;

    public MissionAttemptStartedResult(DefaultGame cardGame, String performingPlayerId, AttemptMissionAction action,
                                       MissionCard mission) {
        super(cardGame, ActionResultType.MISSION_ATTEMPT_STARTED, performingPlayerId, action);
        _mission = mission;
    }

}