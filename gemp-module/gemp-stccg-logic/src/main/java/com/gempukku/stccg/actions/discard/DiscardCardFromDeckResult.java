package com.gempukku.stccg.actions.discard;

import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

public class DiscardCardFromDeckResult extends ActionResult {
    private final PhysicalCard _card;

    public DiscardCardFromDeckResult(PhysicalCard source, PhysicalCard card) {
        super(Type.FOR_EACH_DISCARDED_FROM_DECK, source);
        _card = card;
    }


    public PhysicalCard getDiscardedCard() {
        return _card;
    }
}