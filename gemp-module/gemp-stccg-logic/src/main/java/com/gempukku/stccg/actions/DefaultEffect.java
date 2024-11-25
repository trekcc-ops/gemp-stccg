package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;

public abstract class DefaultEffect implements Effect {
    private Boolean _carriedOut;
    protected boolean _prevented;
    protected final String _performingPlayerId;
    protected final DefaultGame _game;

    protected DefaultEffect(DefaultGame game, String performingPlayerId) {
        _performingPlayerId = performingPlayerId;
        _game = game;
    }

    protected DefaultEffect(Player player) {
        _performingPlayerId = player.getPlayerId();
        _game = player.getGame();
    }

    protected DefaultEffect(ActionContext actionContext, String playerId) {
        _performingPlayerId = playerId;
        _game = actionContext.getGame();
    }

    protected DefaultEffect(ActionContext actionContext) {
        _performingPlayerId = actionContext.getPerformingPlayerId();
        _game = actionContext.getGame();
    }

    protected DefaultEffect(PhysicalCard card) {
        _performingPlayerId = card.getOwnerName();
        _game = card.getGame();
    }

    protected DefaultEffect(DefaultGame game, Action action) {
        _performingPlayerId = action.getPerformingPlayerId();
        _game = game;
    }


    protected abstract FullEffectResult playEffectReturningResult();

    @Override
    public final void playEffect() {
        FullEffectResult fullEffectResult = playEffectReturningResult();
        _carriedOut = fullEffectResult.isCarriedOut();
    }

    @Override
    public boolean wasCarriedOut() {
        if (_carriedOut == null)
            throw new IllegalStateException("Effect has to be played first");
        return _carriedOut && !_prevented;
    }

    public String getText() { return null; }

    @Override
    public EffectType getType() {
        return null;
    }

    public static class FullEffectResult {
        private final boolean _carriedOut;

        public FullEffectResult(boolean carriedOut) {
            _carriedOut = carriedOut;
        }

        public boolean isCarriedOut() {
            return _carriedOut;
        }
    }

    public String getPerformingPlayerId() {
        return _performingPlayerId; }

    public DefaultGame getGame() { return _game; }

}