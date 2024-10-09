package com.gempukku.stccg.actions.discard;

import com.gempukku.stccg.actions.EffectResult;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

public class DiscardCardsFromPlayResult extends EffectResult {
    private final String _performingPlayer;
    private final PhysicalCard _card;

    public DiscardCardsFromPlayResult(PhysicalCard source, String performingPlayer, PhysicalCard card) {
        super(EffectResult.Type.FOR_EACH_DISCARDED_FROM_PLAY, source);
        _performingPlayer = performingPlayer;
        _card = card;
    }

    public String getPerformingPlayer() {
        return _performingPlayer;
    }

    public PhysicalCard getDiscardedCard() {
        return _card;
    }

}
