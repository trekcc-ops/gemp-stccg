package com.gempukku.stccg.results;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.effects.EffectResult;

public class DiscardCardFromPlayPileResult extends EffectResult {
    private final PhysicalCard _source;
    private final PhysicalCard _card;

    public DiscardCardFromPlayPileResult(PhysicalCard source, PhysicalCard card) {
        super(Type.FOR_EACH_DISCARDED_FROM_PLAY_PILE);
        _source = source;
        _card = card;
    }

    public PhysicalCard getSource() {
        return _source;
    }

    public PhysicalCard getDiscardedCard() {
        return _card;
    }
}