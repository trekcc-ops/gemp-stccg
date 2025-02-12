package com.gempukku.stccg.cards.cardgroup;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;

import java.util.LinkedList;
import java.util.List;

public class DrawDeck extends CardPile {

    @JsonProperty("cardCount")
    public int size() {
        return _cards.size();
    }

    public void remove(PhysicalCard card) {
        _cards.remove(card);
        card.setZone(Zone.VOID);
    }

}