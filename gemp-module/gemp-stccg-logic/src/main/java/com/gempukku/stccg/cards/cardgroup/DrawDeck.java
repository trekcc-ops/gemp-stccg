package com.gempukku.stccg.cards.cardgroup;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;

public class DrawDeck extends CardPile<PhysicalCard> {

    @JsonProperty("cardCount")
    public int size() {
        return _cards.size();
    }

    public void addCardToTop(PhysicalCard card) {
        _cards.add(card);
        card.setZone(Zone.DRAW_DECK);
    }

    public void remove(PhysicalCard card) {
        _cards.remove(card);
        card.setZone(Zone.VOID);
    }

}