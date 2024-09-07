package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

public abstract class DefaultEffect implements Effect {
    protected Action _action;
    private Boolean _carriedOut;
    protected boolean _prevented;
    protected final String _performingPlayerId;

    protected DefaultEffect(String performingPlayerId) { _performingPlayerId = performingPlayerId; }
    protected DefaultEffect(ActionContext actionContext) {
        _performingPlayerId = actionContext.getPerformingPlayerId();
    }

    protected DefaultEffect(PhysicalCard card) { _performingPlayerId = card.getOwnerName(); }
    protected DefaultEffect(Action action) {
        _action = action;
        _performingPlayerId = action.getPerformingPlayerId();
    }
    protected DefaultEffect() { _performingPlayerId = null; } // Should be reserved for automatic game effects

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

    @Override
    public String getText() {
        return null;
    }

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

}