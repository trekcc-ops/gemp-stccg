package com.gempukku.stccg.filters;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.MissionLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DestinationBlueprint {

    private final Affiliation _affiliation;

    public DestinationBlueprint(@JsonProperty(value = "affiliation", required = true) Affiliation affiliation) {
        _affiliation = affiliation;
    }

    public Collection<MissionLocation> getDestinationOptions(ST1EGame stGame, String performingPlayerName) {
        Collection<MissionLocation> result = new ArrayList<>();
        for (MissionLocation location : stGame.getGameState().getSpacelineLocations()) {
            if (location.hasMatchingAffiliationIcon(stGame, performingPlayerName, List.of(_affiliation))) {
                result.add(location);
            }
        }
        return result;
    }

}