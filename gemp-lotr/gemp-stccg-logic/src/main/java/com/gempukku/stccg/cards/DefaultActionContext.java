package com.gempukku.stccg.cards;

import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.results.EffectResult;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Objects;

public class DefaultActionContext extends GenericActionContext {
    private final DefaultGame _game;
    protected final ActionContext _relevantContext;

    public DefaultActionContext(String performingPlayer, DefaultGame game, PhysicalCard source,
                                EffectResult effectResult, Effect effect) {
        this(null, performingPlayer, game, source, effectResult, effect);
    }

    public DefaultActionContext(ActionContext delegate, String performingPlayer, DefaultGame game,
                                 PhysicalCard source, EffectResult effectResult, Effect effect) {
        super(performingPlayer, source, effectResult, effect);
        _game = game;
        _relevantContext = Objects.requireNonNullElse(delegate, this);
    }

    @Override
    protected ActionContext getRelevantContext() { return _relevantContext; }

    @Override
    public DefaultGame getGame() {
        return _game;
    }

}
