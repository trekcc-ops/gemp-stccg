package com.gempukku.stccg.effects.defaulteffect;

import com.gempukku.stccg.effects.DefaultEffect;

public abstract class UnrespondableEffect extends DefaultEffect {

    protected abstract void doPlayEffect();

    @Override
    public boolean isPlayableInFull() {
        return true;
    }

    @Override
    public FullEffectResult playEffectReturningResult() {
        doPlayEffect();
        return new FullEffectResult(true);
    }
}
