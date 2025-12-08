package com.gempukku.stccg.cards.cardgroup;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;

public class DiscardPile extends CardPile<PhysicalCard> {

    @JsonIgnore
    public int size() {
        return super.size();
    }

    public void remove(PhysicalCard card) {
        _cards.remove(card);
        card.setZone(Zone.VOID);
    }

}