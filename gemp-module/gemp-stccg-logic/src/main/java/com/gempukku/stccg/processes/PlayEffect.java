package com.gempukku.stccg.processes;

import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.UnrespondableEffect;

class PlayEffect extends UnrespondableEffect {
    private final Effect _effect;

    PlayEffect(Effect effect) {
        super(effect.getGame());
        _effect = effect;
    }

    @Override
    protected void doPlayEffect() {
        _effect.playEffect();
    }
}