package com.gempukku.stccg.actions.discard;

import com.gempukku.stccg.actions.EffectResult;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.actions.Effect;

public class DiscardCardFromPlayPileResult extends EffectResult {
    private final PhysicalCard _card;

    public DiscardCardFromPlayPileResult(Effect effect, PhysicalCard source, PhysicalCard card) {
        super(Type.FOR_EACH_DISCARDED_FROM_PLAY_PILE, effect, source);
        _card = card;
    }

    public PhysicalCard getSource() {
        return _source;
    }

    public PhysicalCard getDiscardedCard() {
        return _card;
    }
}