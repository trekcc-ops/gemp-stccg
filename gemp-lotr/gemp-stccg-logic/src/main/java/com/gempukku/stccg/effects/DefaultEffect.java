package com.gempukku.stccg.effects;

import com.gempukku.stccg.effects.utils.EffectType;

public abstract class DefaultEffect implements Effect {
    private Boolean _carriedOut;
    protected boolean _prevented;

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

    protected static class FullEffectResult {
        private final boolean _carriedOut;

        public FullEffectResult(boolean carriedOut) {
            _carriedOut = carriedOut;
        }

        public boolean isCarriedOut() {
            return _carriedOut;
        }
    }



}