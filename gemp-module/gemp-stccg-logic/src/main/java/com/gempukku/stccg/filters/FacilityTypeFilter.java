package com.gempukku.stccg.filters;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.FacilityType;
import com.gempukku.stccg.game.DefaultGame;

public class FacilityTypeFilter implements CardFilter {

    @JsonProperty("facilityType")
    private final FacilityType _facilityType;

    @JsonCreator
    public FacilityTypeFilter(@JsonProperty(value = "facilityType", required = true) FacilityType facilityType) {
        _facilityType = facilityType;
    }

    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        return physicalCard.getBlueprint().getFacilityType() == _facilityType;
    }
}