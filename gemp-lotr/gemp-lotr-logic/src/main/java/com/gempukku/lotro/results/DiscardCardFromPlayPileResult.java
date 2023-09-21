package com.gempukku.lotro.results;

import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.effects.EffectResult;

public class DiscardCardFromPlayPileResult extends EffectResult {
    private final LotroPhysicalCard _source;
    private final LotroPhysicalCard _card;

    public DiscardCardFromPlayPileResult(LotroPhysicalCard source, LotroPhysicalCard card) {
        super(Type.FOR_EACH_DISCARDED_FROM_PLAY_PILE);
        _source = source;
        _card = card;
    }

    public LotroPhysicalCard getSource() {
        return _source;
    }

    public LotroPhysicalCard getDiscardedCard() {
        return _card;
    }
}