package com.gempukku.stccg.cards;

import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.results.EffectResult;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Objects;

public class DefaultActionContext extends GenericActionContext {
    private final DefaultGame _game;
    protected final ActionContext _relevantContext;

    public DefaultActionContext(PhysicalCard source) {
        this(source.getOwnerName(), source.getGame(), source, null, null);
    }

    public DefaultActionContext(String playerId, PhysicalCard source) {
        this(playerId, source.getGame(), source, null, null);
    }

    public DefaultActionContext(DefaultGame game, PhysicalCard source, Effect effect) {
        this(source.getOwnerName(), game, source, null, effect);
    }

    public DefaultActionContext(PhysicalCard source, EffectResult effectResult) {
        this(source.getOwnerName(), source.getGame(), source, effectResult, null);
    }

    public DefaultActionContext(String playerId, DefaultGame game, PhysicalCard source) {
        this(playerId, game, source, null, null);
    }

    public DefaultActionContext(String performingPlayer, DefaultGame game, PhysicalCard source,
                                EffectResult effectResult, Effect effect) {
        this(null, performingPlayer, game, source, effectResult, effect);
    }

    public DefaultActionContext(ActionContext delegate, Effect effect) {
        this(delegate, delegate.getPerformingPlayer(), delegate.getGame(), delegate.getSource(), null, effect);
    }

    public DefaultActionContext(ActionContext delegate, EffectResult effectResult) {
        this(delegate, delegate.getPerformingPlayer(), delegate.getGame(), delegate.getSource(), effectResult, null);
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

    @Override
    public GameState getGameState() { return _game.getGameState(); }

}
