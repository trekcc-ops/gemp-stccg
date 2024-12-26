package com.gempukku.stccg.cards;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.gamestate.TribblesGameState;
import com.gempukku.stccg.actions.EffectResult;
import com.gempukku.stccg.game.TribblesGame;

import java.util.Objects;

public class TribblesActionContext extends DefaultActionContext {
    private final TribblesGame _game;
    protected final ActionContext _relevantContext;

    public TribblesActionContext(Player performingPlayer, PhysicalCard source) {
        this(null, performingPlayer.getPlayerId(), (TribblesGame) performingPlayer.getGame(),
                source, null, null);
    }


    public TribblesActionContext(String performingPlayer, TribblesGame game, PhysicalCard source,
                                 Effect effect, EffectResult effectResult) {
        this(null, performingPlayer, game, source, effect, effectResult);
    }

    public TribblesActionContext(ActionContext delegate, String performingPlayer, TribblesGame game,
                                 PhysicalCard source, Effect effect, EffectResult effectResult) {
        super(performingPlayer, game, source, effect, effectResult);
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

    @Override
    public ActionContext createDelegateContext(Effect effect) {
        return new TribblesActionContext(this, getPerformingPlayerId(), getGame(), getSource(), effect, null);
    }

    @Override
    public ActionContext createDelegateContext(EffectResult effectResult) {
        return new TribblesActionContext(this, getPerformingPlayerId(), getGame(), getSource(), effect, effectResult);
    }


}