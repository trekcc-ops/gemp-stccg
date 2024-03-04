package com.gempukku.stccg.actions.missionattempt;

import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.cards.physicalcard.PhysicalMissionCard;
import com.gempukku.stccg.cards.AttemptingEntity;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.requirement.missionrequirements.MissionRequirement;

public class AttemptMissionEffect extends DefaultEffect {
    private final DefaultGame _game;
    private final PhysicalMissionCard _mission;
    private final AttemptingEntity _attemptingEntity;

    public AttemptMissionEffect(Player player, AttemptingEntity attemptingEntity, PhysicalMissionCard missionCard) {
        super(player.getPlayerId());
        _game = player.getGame();
        _mission = missionCard;
        _attemptingEntity = attemptingEntity;
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
        if (requirement.canBeMetBy(_attemptingEntity.getAttemptingPersonnel())) {
            _game.getGameState().sendMessage("DEBUG - Mission solved!");
            _mission.isSolvedByPlayer(_performingPlayerId);
        }
        else _game.getGameState().sendMessage("DEBUG - Mission attempt failed!");
        return new FullEffectResult(true);
    }

}