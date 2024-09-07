package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.UnrespondableEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.DefaultGame;

public class IncrementPhaseLimitEffect extends UnrespondableEffect {
    private final PhysicalCard card;
    private final int limit;
    private Phase phase;
    private final String prefix;
    private final DefaultGame _game;

    public IncrementPhaseLimitEffect(ActionContext actionContext, int limit) {
        this(actionContext.getGame(), actionContext.getSource(), null, "", limit);
    }

    public IncrementPhaseLimitEffect(ActionContext actionContext, Phase phase, int limit) {
        this(actionContext.getGame(), actionContext.getSource(), phase, "", limit);
    }

    public IncrementPhaseLimitEffect(ActionContext actionContext, String prefix, int limit) {
        this(actionContext.getGame(), actionContext.getSource(), null, prefix, limit);
    }

    private IncrementPhaseLimitEffect(DefaultGame game, PhysicalCard card, Phase phase, String prefix, int limit) {
        this.card = card;
        this.phase = phase;
        this.prefix = prefix;
        this.limit = limit;
        _game = game;
    }

    @Override
    protected void doPlayEffect() {
        if (phase == null)
            phase = _game.getGameState().getCurrentPhase();
        _game.getModifiersQuerying().getUntilEndOfPhaseLimitCounter(card, prefix, phase)
                .incrementToLimit(limit, 1);
    }
}
