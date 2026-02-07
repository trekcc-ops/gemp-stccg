package com.gempukku.stccg.actions.discard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

import java.util.List;

public class DiscardCardFromPlayResult extends ActionResult {
    protected final PhysicalCard _discardedCard;

    public DiscardCardFromPlayResult(PhysicalCard card, List<Type> types, Action action) {
        super(types, action);
        _discardedCard = card;
    }

    public DiscardCardFromPlayResult(PhysicalCard card, Action action) {
        this(card, List.of(Type.JUST_DISCARDED_FROM_PLAY), action);
    }




    public String getPerformingPlayer() {
        return _performingPlayerId;
    }

    public PhysicalCard getDiscardedCard() {
        return _discardedCard;
    }

}