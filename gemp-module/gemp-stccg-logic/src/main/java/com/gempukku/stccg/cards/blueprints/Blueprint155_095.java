package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.cards.physicalcard.PhysicalNounCard1E;
import com.gempukku.stccg.common.filterable.Affiliation;


public class Blueprint155_095 extends CardBlueprint {
    Blueprint155_095() {
        super("155_095"); // Will Riker (The Next Generation)
    }

    @Override
    public boolean doesNotWorkWithPerRestrictionBox(PhysicalNounCard1E thisCard, PhysicalNounCard1E otherCard) {
        return (otherCard.getCurrentAffiliation() == Affiliation.FEDERATION && thisCard.getCardId() != otherCard.getCardId());
    }
}