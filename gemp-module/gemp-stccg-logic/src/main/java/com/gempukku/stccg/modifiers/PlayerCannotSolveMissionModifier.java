package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.gamestate.MissionLocation;

public class PlayerCannotSolveMissionModifier extends AbstractModifier {
    private final String _playerId;
    private final int _locationId;

    public PlayerCannotSolveMissionModifier(MissionLocation missionLocation, String playerId) {
        super(ModifierEffect.SOLVE_MISSION_MODIFIER);
        _playerId = playerId;
        _locationId = missionLocation.getLocationId();
    }

    public boolean cannotSolveMission(MissionLocation mission, String playerId) {
        return (_playerId.equals(playerId) && _locationId == mission.getLocationId());
    }


}