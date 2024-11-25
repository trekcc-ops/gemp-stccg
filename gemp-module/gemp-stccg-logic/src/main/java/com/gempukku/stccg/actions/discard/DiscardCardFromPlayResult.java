package com.gempukku.stccg.actions.discard;

import com.gempukku.stccg.actions.EffectResult;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

public class DiscardCardFromPlayResult extends EffectResult {
    private final PhysicalCard _card;

    public DiscardCardFromPlayResult(PhysicalCard source, PhysicalCard card) {
        super(EffectResult.Type.FOR_EACH_DISCARDED_FROM_PLAY, source);
        _card = card;
    }

    public String getPerformingPlayer() {
        return _performingPlayerId;
    }

    public PhysicalCard getDiscardedCard() {
        return _card;
    }

}