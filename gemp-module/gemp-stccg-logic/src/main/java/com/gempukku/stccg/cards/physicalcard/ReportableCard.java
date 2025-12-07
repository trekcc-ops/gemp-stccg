package com.gempukku.stccg.cards.physicalcard;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.game.ST1EGame;

public interface ReportableCard extends CardWithCompatibility {

    default boolean canReportToFacility(FacilityCard facility, ST1EGame stGame) {
        if (getCardType() == CardType.EQUIPMENT && facility.isUsableBy(getOwnerName()))
            return true;
        if (this instanceof AffiliatedCard affiliatedCard) {
            for (Affiliation affiliation : affiliatedCard.getAffiliationOptions())
                if (affiliatedCard.canReportToFacilityAsAffiliation(facility, affiliation, stGame))
                    return true;
        }
        return false;
    }

    @JsonProperty("isStopped")
    boolean isStopped();

}