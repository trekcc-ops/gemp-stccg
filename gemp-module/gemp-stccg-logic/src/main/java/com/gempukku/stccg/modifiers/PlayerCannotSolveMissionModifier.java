package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.physicalcard.MissionCard;

public class PlayerCannotSolveMissionModifier extends AbstractModifier {
    private final String _playerId;
    private final MissionCard _mission;

    public PlayerCannotSolveMissionModifier(MissionCard mission, String playerId) {
        super(mission.getGame(), ModifierEffect.SOLVE_MISSION_MODIFIER);
        _playerId = playerId;
        _mission = mission;
    }

    public boolean cannotSolveMission(MissionCard mission, String playerId) {
        return (_playerId.equals(playerId) && _mission == mission);
    }

}