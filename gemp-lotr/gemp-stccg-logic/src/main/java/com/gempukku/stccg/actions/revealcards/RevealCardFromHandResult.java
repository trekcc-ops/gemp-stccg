package com.gempukku.stccg.actions.revealcards;

import com.gempukku.stccg.actions.EffectResult;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

public class RevealCardFromHandResult extends EffectResult {
    private final String _playerId;
    private final PhysicalCard _card;

    public RevealCardFromHandResult(PhysicalCard source, String playerId, PhysicalCard card) {
        super(Type.FOR_EACH_REVEALED_FROM_HAND, source);
        _playerId = playerId;
        _card = card;
    }

    public PhysicalCard getSource() {
        return _source;
    }

    public String getPlayerId() {
        return _playerId;
    }

    public PhysicalCard getRevealedCard() {
        return _card;
    }
}
