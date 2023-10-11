package com.gempukku.stccg.at.effects;

import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.effects.defaulteffect.UnrespondableEffect;

import java.util.concurrent.atomic.AtomicInteger;

public class IncrementEffect extends UnrespondableEffect {
    private final AtomicInteger _atomicInteger;

    public IncrementEffect(AtomicInteger atomicInteger) {
        _atomicInteger = atomicInteger;
    }

    @Override
    protected void doPlayEffect() {
        _atomicInteger.incrementAndGet();
    }
}
