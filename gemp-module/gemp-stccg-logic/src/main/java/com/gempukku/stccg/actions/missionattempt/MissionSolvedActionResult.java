package com.gempukku.stccg.actions.missionattempt;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;

public class MissionSolvedActionResult extends ActionResult {

    private final boolean _missionSpecialistHelped;

    public MissionSolvedActionResult(String performingPlayerId, Action action, boolean missionSpecialistHelped) {
        super(Type.SOLVE_MISSION, performingPlayerId, action);
        _missionSpecialistHelped = missionSpecialistHelped;
    }

    public boolean didMissionSpecialistHelp() {
        return _missionSpecialistHelped;
    }
}