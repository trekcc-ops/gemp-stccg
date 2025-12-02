package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.physicalcard.AffiliatedCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;

public class MatchingAffiliationFilter implements CardFilter {

    private final Collection<PhysicalCard> _cardsToMatch;

    public MatchingAffiliationFilter(Collection<PhysicalCard> cardsToMatch) {
        _cardsToMatch = cardsToMatch;
    }
    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        boolean matching = true;
        for (PhysicalCard cardToMatch : _cardsToMatch) {
            if (physicalCard instanceof AffiliatedCard affilCard1 && cardToMatch instanceof AffiliatedCard affilCard2) {
                if (!affilCard1.matchesAffiliationOf(affilCard2))
                    matching = false;
            }
        }
        return matching;
    }
}