package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;

public class InZoneFilter implements CardFilter {

    private final Zone _zone;

    public InZoneFilter(Zone zone) {
        _zone = zone;
    }
    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        return physicalCard.getZone() == _zone;
    }
}