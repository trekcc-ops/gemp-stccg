package com.gempukku.lotro.results;

import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.effects.EffectResult;

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
