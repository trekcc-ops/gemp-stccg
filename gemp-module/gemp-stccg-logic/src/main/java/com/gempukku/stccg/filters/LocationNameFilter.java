package com.gempukku.stccg.filters;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.GameLocation;

import java.util.Objects;

public class LocationNameFilter implements CardFilter {

    @JsonProperty("locationName")
    private final String _locationName;

    @JsonCreator
    public LocationNameFilter(@JsonProperty("locationName") String locationName) {
        _locationName = locationName;
    }
    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        if (game instanceof ST1EGame stGame) {
            GameLocation location = physicalCard.getGameLocation(stGame);
            return Objects.equals(location.getLocationName(), _locationName);
        } else {
            return false;
        }
    }
}