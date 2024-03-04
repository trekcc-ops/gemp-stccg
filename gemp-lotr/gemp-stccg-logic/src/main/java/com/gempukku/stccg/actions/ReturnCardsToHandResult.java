package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

public class ReturnCardsToHandResult extends EffectResult {

    public ReturnCardsToHandResult(PhysicalCard card) {
        super(Type.FOR_EACH_RETURNED_TO_HAND, card);
    }

    public PhysicalCard getReturnedCard() {
        return _source;
    }
}
