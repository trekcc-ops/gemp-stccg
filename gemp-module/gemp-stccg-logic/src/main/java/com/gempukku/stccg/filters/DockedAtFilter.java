package com.gempukku.stccg.filters;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalShipCard;
import com.gempukku.stccg.game.DefaultGame;

public class DockedAtFilter implements CardFilter {

    @JsonProperty("facilityCardId")
    private final int _facilityCardId;

    public DockedAtFilter(FacilityCard facility) {
        _facilityCardId = facility.getCardId();
    }
    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        return physicalCard instanceof PhysicalShipCard shipCard && shipCard.isDockedAtCardId(_facilityCardId);
    }
}