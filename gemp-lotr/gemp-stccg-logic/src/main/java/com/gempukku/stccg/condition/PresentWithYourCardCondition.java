package com.gempukku.stccg.condition;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;

public class PresentWithYourCardCondition implements Condition {
    private final int _count;
    private final Filter _filters;
    private final PhysicalCard _card;

    /**
     * Creates a condition that is fulfilled when the specified card is "present with" your card accepted by the specified filter.
     * @param card the card
     * @param filters the filter
     */
    public PresentWithYourCardCondition(PhysicalCard card, Filterable filters) {
        this(card, 1, filters);
    }

    /**
     * Creates a condition that is fulfilled when the specified card is "present with" at least a specified number of your
     * cards accepted by the specified filter.
     * @param card the card
     * @param count the number of cards
     * @param filters the filter
     */
    public PresentWithYourCardCondition(PhysicalCard card, int count, Filterable filters) {
        _card = card;
        _count = count;
        _filters = Filters.and(filters);
    }

    @Override
    public boolean isFulfilled() {
        return Filters.filterYourCardsPresentWith(_card.getOwner(), _card, _filters).size() >= _count;
    }
}