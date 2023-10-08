package com.gempukku.stccg.results;

import com.gempukku.stccg.common.TribblePower;
import com.gempukku.stccg.effects.EffectResult;

public class ActivateTribblePowerResult extends EffectResult {
    private boolean _effectCancelled;

    public ActivateTribblePowerResult(String activatingPlayer, TribblePower tribblePower) {
        super(Type.ACTIVATE_TRIBBLE_POWER);
    }

    public void cancelEffect() {
        _effectCancelled = true;
    }

    public boolean isEffectCancelled() {
        return _effectCancelled;
    }
}
