package com.gempukku.stccg.requirement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.common.filterable.MissionType;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

public class YouHaveSolvedMissionRequirement implements Requirement {

    private MissionType _missionType;

    @JsonCreator
    private YouHaveSolvedMissionRequirement(@JsonProperty("missionType")MissionType missionType) {
        _missionType = missionType;
    }

    @Override
    public boolean accepts(GameTextContext context, DefaultGame cardGame) {
        try {
            Player player = cardGame.getPlayer(context.yourName());
            if (_missionType == null) {
                return player.hasSolvedMission();
            } else {
                return player.hasSolvedMission(_missionType);
            }
        } catch(PlayerNotFoundException exp) {
            cardGame.sendErrorMessage(exp);
            return false;
        }
    }
}