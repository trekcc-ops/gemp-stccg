package com.gempukku.lotro.results;

import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.effects.EffectResult;

public class RevealCardFromTopOfDeckResult extends EffectResult {
    private final String _playerId;
    private final LotroPhysicalCard _revealedCard;

    public RevealCardFromTopOfDeckResult(String playerId, LotroPhysicalCard revealedCard) {
        super(Type.FOR_EACH_REVEALED_FROM_TOP_OF_DECK);
        _playerId = playerId;
        _revealedCard = revealedCard;
    }

    public String getPlayerId() {
        return _playerId;
    }

    public LotroPhysicalCard getRevealedCard() {
        return _revealedCard;
    }
}
