package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.physicalcard.CardWithStrength;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

import java.util.ArrayList;
import java.util.Collection;

public class HighestStrengthCardFilter implements CardFilter {

    private final CardFilter _otherFilter;

    public HighestStrengthCardFilter(CardFilter otherFilter) {
        _otherFilter = otherFilter;
    }
    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        Collection<PhysicalCard> filteredCards = Filters.filter(game, _otherFilter);
        Collection<PhysicalCard> highestStrengthCards = new ArrayList<>();
        int highestStrength = 0;
        for (PhysicalCard card : filteredCards) {
            if (card instanceof CardWithStrength cardWithAttributes) {
                int total = cardWithAttributes.getStrength(game);
                if (total > highestStrength) {
                    highestStrength = total;
                    highestStrengthCards.clear();
                }
                if (total == highestStrength) {
                    highestStrengthCards.add(card);
                }
            }
        }
        return highestStrengthCards.contains(physicalCard);
    }
}