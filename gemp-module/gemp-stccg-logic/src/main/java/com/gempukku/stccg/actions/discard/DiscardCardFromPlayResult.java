package com.gempukku.stccg.actions.discard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

public class DiscardCardFromPlayResult extends ActionResult {
    private final PhysicalCard _card;

    public DiscardCardFromPlayResult(PhysicalCard card, Action action) {
        super(ActionResult.Type.FOR_EACH_DISCARDED_FROM_PLAY, action);
        _card = card;
    }


    public String getPerformingPlayer() {
        return _performingPlayerId;
    }

    public PhysicalCard getDiscardedCard() {
        return _card;
    }

}