package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.FacilityType;
import com.gempukku.stccg.game.DefaultGame;

public class FacilityTypeFilter implements CardFilter {

    private final FacilityType _facilityType;

    public FacilityTypeFilter(FacilityType facilityType) {
        _facilityType = facilityType;
    }

    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        return physicalCard.getBlueprint().getFacilityType() == _facilityType;
    }
}