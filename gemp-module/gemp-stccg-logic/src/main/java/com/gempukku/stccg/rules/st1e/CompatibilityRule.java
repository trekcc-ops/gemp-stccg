package com.gempukku.stccg.rules.st1e;

import com.gempukku.stccg.cards.physicalcard.AffiliatedCard;
import com.gempukku.stccg.cards.physicalcard.CardWithCompatibility;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.CardType;

import java.util.List;

public class CompatibilityRule {

    public static boolean areCardsCompatible(CardWithCompatibility card1, CardWithCompatibility card2) {
        if (card1.getCardType() == CardType.EQUIPMENT || card2.getCardType() == CardType.EQUIPMENT)
            return true;
        else if (card1 instanceof AffiliatedCard affiliatedCard1 && card2 instanceof AffiliatedCard affiliatedCard2) {
            List<Affiliation> affil1 = affiliatedCard1.getCurrentAffiliations();
            List<Affiliation> affil2 = affiliatedCard2.getCurrentAffiliations();
            boolean result = false;
            for (Affiliation affiliation1 : affil1) {
                for (Affiliation affiliation2 : affil2) {
                    if (areAffiliationsCompatible(affiliation1, affiliation2)) {
                        result = true;
                        break;
                    }
                }
            }
            return result;
        } else {
            return false;
        }
    }

    private static boolean areAffiliationsCompatible(Affiliation affil1, Affiliation affil2) {
        if (affil1 == affil2) {
            return true;
        } else if (affil1 == Affiliation.BORG || affil2 == Affiliation.BORG) {
            return false;
        } else {
            return affil1 == Affiliation.NON_ALIGNED || affil2 == Affiliation.NON_ALIGNED ||
                    affil1 == Affiliation.NEUTRAL || affil2 == Affiliation.NEUTRAL;
        }
    }

}