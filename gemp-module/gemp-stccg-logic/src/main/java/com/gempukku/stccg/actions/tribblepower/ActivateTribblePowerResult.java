package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.EffectResult;

public class ActivateTribblePowerResult extends EffectResult {
    private boolean _effectCancelled;

    public ActivateTribblePowerResult(Effect effect) {
        super(Type.ACTIVATE_TRIBBLE_POWER, effect);
    }

    public void cancelEffect() {
        _effectCancelled = true;
    }

    public boolean isEffectCancelled() {
        return _effectCancelled;
    }
}
