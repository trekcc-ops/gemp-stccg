package com.gempukku.stccg.filters;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class AtLocationFilter implements CardFilter {

    @JsonProperty("locationId")
    private final int _locationId;

    @JsonCreator
    public AtLocationFilter(@JsonProperty("locationId") int locationId) {
        _locationId = locationId;
    }
    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        return _locationId >= 0 && physicalCard.getLocationId() == _locationId;
    }
}