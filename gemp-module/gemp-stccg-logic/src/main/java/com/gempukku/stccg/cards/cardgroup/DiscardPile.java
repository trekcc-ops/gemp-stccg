package com.gempukku.stccg.cards.cardgroup;

import com.fasterxml.jackson.annotation.*;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;

public class DiscardPile extends CardPile {

    @JsonProperty("cardCount")
    public int size() {
        return _cards.size();
    }

    public void remove(PhysicalCard card) {
        _cards.remove(card);
        card.setZone(Zone.VOID);
    }

}