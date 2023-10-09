package com.gempukku.stccg.effects;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.DefaultGame;

public class IncrementPhaseLimitEffect extends UnrespondableEffect {
    private final PhysicalCard card;
    private final int limit;
    private Phase phase;
    private final String prefix;

    public IncrementPhaseLimitEffect(PhysicalCard card, int limit) {
        this(card, null, "", limit);
    }

    public IncrementPhaseLimitEffect(PhysicalCard card, Phase phase, int limit) {
        this(card, phase, "", limit);
    }

    public IncrementPhaseLimitEffect(PhysicalCard card, String prefix, int limit) {
        this(card, null, prefix, limit);
    }

    private IncrementPhaseLimitEffect(PhysicalCard card, Phase phase, String prefix, int limit) {
        this.card = card;
        this.phase = phase;
        this.prefix = prefix;
        this.limit = limit;
    }

    @Override
    protected void doPlayEffect(DefaultGame game) {
        if (phase == null)
            phase = game.getGameState().getCurrentPhase();
        game.getModifiersQuerying().getUntilEndOfPhaseLimitCounter(card, prefix, phase).incrementToLimit(limit, 1);
    }
}
