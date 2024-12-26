package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.actions.Effect;

public class PlayEffect extends DefaultEffect {
    private final Effect _effect;

    public PlayEffect(Effect effect) {
        super(effect);
        _effect = effect;
    }

    @Override
    public boolean isPlayableInFull() {
        return true;
    }

    @Override
    public FullEffectResult playEffectReturningResult() {
        _effect.playEffect();
        return new FullEffectResult(true);
    }

}