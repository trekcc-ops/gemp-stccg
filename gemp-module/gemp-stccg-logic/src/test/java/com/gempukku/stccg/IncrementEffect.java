package com.gempukku.stccg;


import com.gempukku.stccg.actions.UnrespondableEffect;

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
