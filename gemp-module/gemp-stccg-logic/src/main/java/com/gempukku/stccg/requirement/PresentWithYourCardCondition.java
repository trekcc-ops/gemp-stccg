package com.gempukku.stccg.requirement;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

public class PresentWithYourCardCondition implements Condition {

    @JsonProperty("count")
    private final int _count;

    @JsonProperty("filters")
    private final CardFilter _filters;

    @JsonProperty("cardId")
    private final int _cardId;

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
        _cardId = card.getCardId();
        _count = count;
        _filters = Filters.and(filters);
    }

    @Override
    public boolean isFulfilled(DefaultGame cardGame) {
        try {
            PhysicalCard sourceCard = cardGame.getCardFromCardId(_cardId);
            return Filters.filterYourCardsPresentWith(cardGame, sourceCard.getOwnerName(), sourceCard, _filters).size() >= _count;
        } catch(CardNotFoundException exp) {
            cardGame.sendErrorMessage(exp);
        }
        return false;
    }
}