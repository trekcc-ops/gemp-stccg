package com.gempukku.stccg.filters;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ShipCard;
import com.gempukku.stccg.game.DefaultGame;

public class DockedAtFilter implements CardFilter {

    @JsonProperty("facilityCardId")
    private final int _facilityCardId;

    @JsonCreator
    private DockedAtFilter(@JsonProperty(value = "facilityCardId", required = true) int facilityCardId) {
        _facilityCardId = facilityCardId;
    }

    public DockedAtFilter(FacilityCard facility) {
        this(facility.getCardId());
    }
    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        return physicalCard instanceof ShipCard shipCard && shipCard.isDockedAtCardId(_facilityCardId);
    }
}