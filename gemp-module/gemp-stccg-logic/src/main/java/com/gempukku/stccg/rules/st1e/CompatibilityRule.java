package com.gempukku.stccg.rules.st1e;

import com.gempukku.stccg.cards.physicalcard.AffiliatedCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalNounCard1E;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.CardType;

public class CompatibilityRule {

    public static boolean areCardsCompatible(PhysicalNounCard1E card1, PhysicalNounCard1E card2) {
        if (card1.getCardType() == CardType.EQUIPMENT || card2.getCardType() == CardType.EQUIPMENT)
            return true;
        else if (card1 instanceof AffiliatedCard affiliatedCard1 && card2 instanceof AffiliatedCard affiliatedCard2) {
            Affiliation affil1 = affiliatedCard1.getCurrentAffiliation();
            Affiliation affil2 = affiliatedCard2.getCurrentAffiliation();
            if (affil1 == affil2) {
                return true;
            } else if (affil1 == Affiliation.BORG || affil2 == Affiliation.BORG) {
                return false;
            } else {
                return affil1 == Affiliation.NON_ALIGNED || affil2 == Affiliation.NON_ALIGNED ||
                        affil1 == Affiliation.NEUTRAL || affil2 == Affiliation.NEUTRAL;
            }
        } else {
            return false;
        }
    }

}