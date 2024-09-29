package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.UnrespondableEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

public class IncrementTurnLimitEffect extends UnrespondableEffect {
    private final PhysicalCard card;
    private final int limit;

    public IncrementTurnLimitEffect(ActionContext actionContext, int limit) {
        super(actionContext);
        this.card = actionContext.getSource();
        this.limit = limit;
    }

    @Override
    protected void doPlayEffect() {
        _game.getModifiersQuerying().getUntilEndOfTurnLimitCounter(card).incrementToLimit(limit, 1);
    }
}
