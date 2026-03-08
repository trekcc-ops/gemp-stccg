package com.gempukku.stccg.actions.missionattempt;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class MissionSolvedActionResult extends ActionResult {

    private final boolean _missionSpecialistHelped;
    private final MissionCard _mission;

    public MissionSolvedActionResult(DefaultGame cardGame, String performingPlayerId, Action action,
                                     MissionCard mission, boolean missionSpecialistHelped) {
        super(cardGame, Type.SOLVE_MISSION, performingPlayerId, action);
        _missionSpecialistHelped = missionSpecialistHelped;
        _mission = mission;
    }

    public boolean didMissionSpecialistHelp() {
        return _missionSpecialistHelped;
    }

    public PhysicalCard mission() {
        return _mission;
    }
}