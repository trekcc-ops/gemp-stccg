package com.gempukku.stccg.actions.movecard;

import com.gempukku.stccg.actions.EffectResult;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

public class WhenMoveFromResult extends EffectResult {
    private final PhysicalCard _card;

    public WhenMoveFromResult(PhysicalCard card) {
            // TODO - This isn't a great representation of how moving works in ST:CCG
        super(EffectResult.Type.WHEN_MOVE_FROM, card);
        _card = card;
    }

    public PhysicalCard getMovedFromCard() {
        return _card;
    }
}
