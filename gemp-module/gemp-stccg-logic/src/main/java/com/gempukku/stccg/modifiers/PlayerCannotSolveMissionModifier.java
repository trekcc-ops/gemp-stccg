package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.MissionLocation;

public class PlayerCannotSolveMissionModifier extends AbstractModifier {
    private final String _playerId;
    private final MissionLocation _missionLocation;

    public PlayerCannotSolveMissionModifier(DefaultGame game, MissionLocation missionLocation, String playerId) {
        super(game, ModifierEffect.SOLVE_MISSION_MODIFIER);
        _playerId = playerId;
        _missionLocation = missionLocation;
    }

    public boolean cannotSolveMission(MissionLocation mission, String playerId) {
        return (_playerId.equals(playerId) && _missionLocation == mission);
    }


}