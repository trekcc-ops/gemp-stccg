package com.gempukku.stccg.actions.discard;

import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

public class DiscardCardFromHandResult extends ActionResult {
    private final PhysicalCard _card;
    public DiscardCardFromHandResult(PhysicalCard source, PhysicalCard card) {
        super(Type.FOR_EACH_DISCARDED_FROM_HAND, source);
        _card = card;
    }

    public PhysicalCard getSource() {
        return _source;
    }

    public PhysicalCard getDiscardedCard() {
        return _card;
    }
}