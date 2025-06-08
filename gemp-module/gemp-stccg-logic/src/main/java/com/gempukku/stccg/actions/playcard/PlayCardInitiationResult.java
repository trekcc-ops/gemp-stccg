package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

public class PlayCardInitiationResult extends ActionResult {
    private final PhysicalCard _cardToPlay;

    public PlayCardInitiationResult(Action action, PhysicalCard cardToPlay) {
        super(Type.PLAY_CARD_INITIATION, action);
        _cardToPlay = cardToPlay;
    }

    public PhysicalCard getCardToPlay() { return _cardToPlay; }
}