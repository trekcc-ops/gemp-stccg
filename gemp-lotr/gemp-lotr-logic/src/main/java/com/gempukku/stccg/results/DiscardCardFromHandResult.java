package com.gempukku.stccg.results;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.effects.EffectResult;

public class DiscardCardFromHandResult extends EffectResult {
    private final PhysicalCard _source;
    private final PhysicalCard _card;
    private final String _handPlayerId;
    private final boolean _forced;

    public DiscardCardFromHandResult(PhysicalCard source, PhysicalCard card, String handPlayerId, boolean forced) {
        super(Type.FOR_EACH_DISCARDED_FROM_HAND);
        _source = source;
        _card = card;
        _handPlayerId = handPlayerId;
        _forced = forced;
    }

    public PhysicalCard getSource() {
        return _source;
    }

    public boolean isForced() {
        return _forced;
    }

    public PhysicalCard getDiscardedCard() {
        return _card;
    }
}