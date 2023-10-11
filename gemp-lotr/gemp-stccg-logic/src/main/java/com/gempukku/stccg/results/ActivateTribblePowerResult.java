package com.gempukku.stccg.results;

import com.gempukku.stccg.common.filterable.TribblePower;

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
