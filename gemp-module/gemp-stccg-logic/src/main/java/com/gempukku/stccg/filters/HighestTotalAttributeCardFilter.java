package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.physicalcard.CardWithAttributes;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

import java.util.ArrayList;
import java.util.Collection;

public class HighestTotalAttributeCardFilter implements CardFilter {

    private final CardFilter _otherFilter;

    public HighestTotalAttributeCardFilter(CardFilter otherFilter) {
        _otherFilter = otherFilter;
    }
    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        Collection<PhysicalCard> filteredCards = Filters.filter(game, _otherFilter);
        Collection<PhysicalCard> highestAttributeCards = new ArrayList<>();
        int highestAttributes = 0;
        for (PhysicalCard card : filteredCards) {
            if (card instanceof CardWithAttributes cardWithAttributes) {
                int total = cardWithAttributes.getTotalAttributes(game);
                if (total > highestAttributes) {
                    highestAttributes = total;
                    highestAttributeCards.clear();
                }
                if (total == highestAttributes) {
                    highestAttributeCards.add(card);
                }
            }
        }
        return highestAttributeCards.contains(physicalCard);
    }
}