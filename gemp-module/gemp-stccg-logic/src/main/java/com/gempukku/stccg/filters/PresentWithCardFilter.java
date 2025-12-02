package com.gempukku.stccg.filters;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class PresentWithCardFilter implements CardFilter {

    @JsonProperty("cardId")
    private final int _cardId;

    public PresentWithCardFilter(PhysicalCard card) {
        _cardId = card.getCardId();
    }

    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        try {
            PhysicalCard withCard = game.getCardFromCardId(_cardId);
            return physicalCard.isPresentWith(withCard);
        } catch(CardNotFoundException exp) {
            game.sendErrorMessage(exp);
            return false;
        }
    }
}