package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.UnrespondableEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class IncrementTurnLimitEffect extends UnrespondableEffect {
    private final PhysicalCard card;
    private final int limit;
    private final DefaultGame _game;

    public IncrementTurnLimitEffect(ActionContext actionContext, int limit) {
        this.card = actionContext.getSource();
        _game = actionContext.getGame();
        this.limit = limit;
    }

    @Override
    protected void doPlayEffect() {
        _game.getModifiersQuerying().getUntilEndOfTurnLimitCounter(card).incrementToLimit(limit, 1);
    }
}
