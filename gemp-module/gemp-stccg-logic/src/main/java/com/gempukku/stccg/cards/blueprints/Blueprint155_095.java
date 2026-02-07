package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.cards.physicalcard.AffiliatedCard;
import com.gempukku.stccg.common.filterable.Affiliation;


public class Blueprint155_095 extends CardBlueprint {
    Blueprint155_095() {
        super("155_095"); // Will Riker (The Next Generation)
    }

    @Override
    public boolean doesNotWorkWithPerRestrictionBox(AffiliatedCard thisCard, AffiliatedCard otherCard) {
        return (!otherCard.isAffiliation(Affiliation.FEDERATION) && thisCard.getCardId() != otherCard.getCardId());
    }
}