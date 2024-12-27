package com.gempukku.stccg.actions.placecard;

import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

public class ReturnCardsToHandResult extends ActionResult {

    public ReturnCardsToHandResult(PhysicalCard card) {
        super(Type.FOR_EACH_RETURNED_TO_HAND, card);
    }

    public PhysicalCard getReturnedCard() {
        return _source;
    }
}