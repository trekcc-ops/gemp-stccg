package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalShipCard;
import com.gempukku.stccg.game.DefaultGame;

public class DockedAtFilter implements CardFilter {

    private final FacilityCard _facility;

    public DockedAtFilter(FacilityCard facility) {
        _facility = facility;
    }
    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        return physicalCard instanceof PhysicalShipCard shipCard && shipCard.isDockedAt(_facility);
    }
}