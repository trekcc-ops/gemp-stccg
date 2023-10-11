package com.gempukku.stccg.results;

import com.gempukku.stccg.cards.PhysicalCard;

public class ReturnCardsToHandResult extends EffectResult {
    private final PhysicalCard _card;

    public ReturnCardsToHandResult(PhysicalCard card) {
        super(Type.FOR_EACH_RETURNED_TO_HAND);
        _card = card;
    }

    public PhysicalCard getReturnedCard() {
        return _card;
    }
}
