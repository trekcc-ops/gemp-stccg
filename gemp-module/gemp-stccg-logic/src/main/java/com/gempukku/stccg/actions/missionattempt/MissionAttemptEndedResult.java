package com.gempukku.stccg.actions.missionattempt;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.ActionResultType;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class MissionAttemptEndedResult extends ActionResult {

    private final boolean _missionSpecialistHelped;
    private final MissionCard _mission;
    private final boolean _wasSuccessful;

    public MissionAttemptEndedResult(DefaultGame cardGame, boolean wasSuccessful, Action action,
                                     MissionCard mission, boolean missionSpecialistHelped) {
        super(cardGame, ActionResultType.MISSION_ATTEMPT_ENDED, action);
        _missionSpecialistHelped = missionSpecialistHelped;
        _mission = mission;
        _wasSuccessful = wasSuccessful;
    }

    @JsonIgnore
    public boolean didMissionSpecialistHelp() {
        return _missionSpecialistHelped;
    }

    @JsonProperty("targetCardId")
    @JsonIdentityReference(alwaysAsId=true)
    public PhysicalCard mission() {
        return _mission;
    }

    @JsonProperty("wasSuccessful")
    public boolean wasSuccessful() {
        return _wasSuccessful; }
}