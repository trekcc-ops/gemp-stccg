package com.gempukku.stccg.actions.missionattempt;

import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.condition.missionrequirements.MissionRequirement;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.gamestate.ST1EMission;

public class AttemptMissionEffect extends DefaultEffect {
    private final AttemptingUnit _attemptingUnit;
    private final ST1EMission _mission;

    public AttemptMissionEffect(Player player, AttemptingUnit attemptingUnit, ST1EMission mission) {
        super(player);
        _attemptingUnit = attemptingUnit;
        _mission = mission;
    }

    @Override
    public String getText() {
        return "Attempt mission";
    }

    @Override
    public boolean isPlayableInFull() {
        return true;
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        // TODO - Pretty much skipping all the actual pieces of attempting a mission except attempting to solve it
        // TODO - NEver mind, didn't actually add that either
        MissionRequirement requirement = _mission.getMissionRequirements(_performingPlayerId);
        if (requirement.canBeMetBy(_attemptingUnit.getAttemptingPersonnel())) {
            _game.sendMessage("DEBUG - Mission solved!");
            _mission.isSolvedByPlayer(_performingPlayerId);
        }
        else _game.sendMessage("DEBUG - Mission attempt failed!");
        return new FullEffectResult(true);
    }

}