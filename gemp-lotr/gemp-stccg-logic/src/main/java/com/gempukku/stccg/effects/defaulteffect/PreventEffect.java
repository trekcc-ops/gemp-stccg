package com.gempukku.stccg.effects.defaulteffect;

import com.gempukku.stccg.effects.DefaultEffect;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Preventable;

public class PreventEffect extends DefaultEffect {
    private final Preventable _preventable;
    private final DefaultGame _game;

    public PreventEffect(DefaultGame game, Preventable preventable) {
        _preventable = preventable;
        _game = game;
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        if (isPlayableInFull()) {
            _preventable.prevent();
            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }

    @Override
    public boolean isPlayableInFull() {
        return !_preventable.isPrevented();
    }
}
