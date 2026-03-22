package com.gempukku.stccg.filters;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;

public class InZoneFilter implements CardFilter {

    @JsonProperty("zone")
    private final Zone _zone;

    public InZoneFilter(Zone zone) {
        _zone = zone;
    }
    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        return physicalCard.getZone() == _zone;
    }
}