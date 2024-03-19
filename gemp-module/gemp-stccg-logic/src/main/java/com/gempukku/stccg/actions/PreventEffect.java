package com.gempukku.stccg.actions;

import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Preventable;

public class PreventEffect extends DefaultEffect {
    private final Preventable _preventable;

    public PreventEffect(DefaultGame game, Preventable preventable) {
        super("dummy value here"); // TODO - Preventable has no player associated??
        _preventable = preventable;
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
