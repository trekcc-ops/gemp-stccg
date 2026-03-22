package com.gempukku.stccg.filters;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.game.DefaultGame;

public class CardTypeFilter implements CardFilter {

    @JsonProperty("cardType")
    private final CardType _cardType;

    @JsonCreator
    public CardTypeFilter(@JsonProperty("cardType") CardType cardType) {
        _cardType = cardType;
    }

    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        return physicalCard.getCardType() == _cardType;
    }
}