package com.gempukku.stccg;


import com.gempukku.stccg.actions.UnrespondableEffect;
import com.gempukku.stccg.game.DefaultGame;

import java.util.concurrent.atomic.AtomicInteger;

public class IncrementEffectForTesting extends UnrespondableEffect {
    private final AtomicInteger _atomicInteger;

    public IncrementEffectForTesting(DefaultGame game, AtomicInteger atomicInteger) {
        super(game);
        _atomicInteger = atomicInteger;
    }

    @Override
    protected void doPlayEffect() {
        _atomicInteger.incrementAndGet();
    }
}
