package com.gempukku.stccg.filters;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.MissionLocation;

public class OnPlanetMissionFilter implements CardFilter {

    @JsonProperty("missionSeedingPlayerName")
    String _missionSeedingPlayerName;

    @JsonCreator
    public OnPlanetMissionFilter(
            @JsonProperty("missionSeedingPlayerName")
            String missionSeedingPlayerName) {
        _missionSeedingPlayerName = missionSeedingPlayerName;
    }

    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        return physicalCard.isOnPlanet(game) &&
                game instanceof ST1EGame stGame &&
                physicalCard.getGameLocation(stGame) instanceof MissionLocation location &&
                location.wasSeededBy(_missionSeedingPlayerName);
    }
}