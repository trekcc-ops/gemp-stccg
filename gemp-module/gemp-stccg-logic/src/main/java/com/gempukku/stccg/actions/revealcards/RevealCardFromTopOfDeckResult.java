package com.gempukku.stccg.actions.revealcards;

import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

public class RevealCardFromTopOfDeckResult extends ActionResult {
    private final String _playerId;
    private final PhysicalCard _revealedCard;

    public RevealCardFromTopOfDeckResult(String playerId, PhysicalCard revealedCard) {
        super(Type.FOR_EACH_REVEALED_FROM_TOP_OF_DECK, revealedCard);
        _playerId = playerId;
        _revealedCard = revealedCard;
    }

    public String getPlayerId() {
        return _playerId;
    }

    public PhysicalCard getRevealedCard() {
        return _revealedCard;
    }
}