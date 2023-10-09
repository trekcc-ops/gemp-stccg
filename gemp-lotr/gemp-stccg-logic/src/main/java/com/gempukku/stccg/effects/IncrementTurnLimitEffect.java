package com.gempukku.stccg.effects;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class IncrementTurnLimitEffect extends UnrespondableEffect {
    private final PhysicalCard card;
    private final int limit;

    public IncrementTurnLimitEffect(PhysicalCard card, int limit) {
        this.card = card;
        this.limit = limit;
    }

    @Override
    protected void doPlayEffect(DefaultGame game) {
        game.getModifiersQuerying().getUntilEndOfTurnLimitCounter(card).incrementToLimit(limit, 1);
    }
}
