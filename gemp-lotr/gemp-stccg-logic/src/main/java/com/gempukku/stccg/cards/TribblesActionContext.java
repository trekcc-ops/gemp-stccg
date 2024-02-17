package com.gempukku.stccg.cards;

import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.gamestate.TribblesGameState;
import com.gempukku.stccg.results.EffectResult;
import com.gempukku.stccg.game.TribblesGame;

import java.util.Objects;

public class TribblesActionContext extends GenericActionContext {
    private final TribblesGame _game;
    protected final ActionContext _relevantContext;

    public TribblesActionContext(String performingPlayer, TribblesGame game, PhysicalCard source,
                                 EffectResult effectResult, Effect effect) {
        this(null, performingPlayer, game, source, effectResult, effect);
    }

    public TribblesActionContext(ActionContext delegate, String performingPlayer, TribblesGame game,
                                 PhysicalCard source, EffectResult effectResult, Effect effect) {
        super(performingPlayer, source, effectResult, effect);
        _game = game;
        _relevantContext = Objects.requireNonNullElse(delegate, this);
    }

    @Override
    protected ActionContext getRelevantContext() { return _relevantContext; }

    @Override
    public TribblesGame getGame() {
        return _game;
    }

    @Override
    public TribblesGameState getGameState() { return _game.getGameState(); }

}
