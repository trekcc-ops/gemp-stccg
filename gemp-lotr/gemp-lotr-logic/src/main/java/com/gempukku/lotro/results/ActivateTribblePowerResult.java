package com.gempukku.lotro.results;

import com.gempukku.lotro.common.TribblePower;
import com.gempukku.lotro.effects.EffectResult;

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
