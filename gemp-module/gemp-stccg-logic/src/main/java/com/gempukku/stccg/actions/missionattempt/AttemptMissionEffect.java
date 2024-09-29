package com.gempukku.stccg.actions.missionattempt;

import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.requirement.missionrequirements.MissionRequirement;

public class AttemptMissionEffect extends DefaultEffect {
    private final MissionCard _mission;
    private final AttemptingUnit _attemptingUnit;

    public AttemptMissionEffect(Player player, AttemptingUnit attemptingUnit, MissionCard missionCard) {
        super(player);
        _mission = missionCard;
        _attemptingUnit = attemptingUnit;
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
        // TODO - NEver mind, didn't actual add that either
        MissionRequirement requirement = _mission.getBlueprint().getMissionRequirements();
        if (requirement.canBeMetBy(_attemptingUnit.getAttemptingPersonnel())) {
            _game.sendMessage("DEBUG - Mission solved!");
            _mission.isSolvedByPlayer(_performingPlayerId);
        }
        else _game.sendMessage("DEBUG - Mission attempt failed!");
        return new FullEffectResult(true);
    }

}