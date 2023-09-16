package com.gempukku.lotro.effects;

import com.gempukku.lotro.game.DefaultGame;

public abstract class UnrespondableEffect extends AbstractEffect {

    protected abstract void doPlayEffect(DefaultGame game);

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        return true;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
        doPlayEffect(game);
        return new FullEffectResult(true);
    }
}
