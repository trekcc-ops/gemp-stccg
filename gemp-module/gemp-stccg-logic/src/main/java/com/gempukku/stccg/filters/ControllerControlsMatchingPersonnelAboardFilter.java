package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.CardWithHullIntegrity;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;

public class ControllerControlsMatchingPersonnelAboardFilter implements CardFilter {
    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        if (physicalCard instanceof CardWithHullIntegrity hullCard) {
            Collection<PersonnelCard> cardsAboard = hullCard.getPersonnelAboard();
            for (PersonnelCard personnel : cardsAboard) {
                if (personnel.matchesAffiliationOf(hullCard) && personnel.hasSameControllerAsCard(game, hullCard))
                    return true;
            }
        }
        return false;
    }
}