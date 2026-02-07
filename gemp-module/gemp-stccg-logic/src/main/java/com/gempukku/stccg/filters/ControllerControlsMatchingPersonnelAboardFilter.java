package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.physicalcard.CardWithHullIntegrity;
import com.gempukku.stccg.cards.physicalcard.AffiliatedCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;

public class ControllerControlsMatchingPersonnelAboardFilter implements CardFilter {
    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        if (physicalCard instanceof CardWithHullIntegrity hullCard &&
                hullCard instanceof AffiliatedCard affiliatedCard) {
            Collection<PersonnelCard> cardsAboard = hullCard.getPersonnelAboard(game);
            for (PersonnelCard personnel : cardsAboard) {
                if (personnel.matchesAffiliationOf(affiliatedCard) && personnel.hasSameControllerAsCard(game, hullCard))
                    return true;
            }
        }
        return false;
    }
}