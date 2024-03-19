package com.gempukku.stccg.actions;

import com.gempukku.stccg.actions.DefaultEffect;

public abstract class UnrespondableEffect extends DefaultEffect {

    protected UnrespondableEffect() {
        super("none"); // TODO - This isn't right but these don't seem like effects with players
    }

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
