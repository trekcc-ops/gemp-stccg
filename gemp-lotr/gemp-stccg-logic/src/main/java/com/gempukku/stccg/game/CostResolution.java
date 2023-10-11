package com.gempukku.stccg.game;

import com.gempukku.stccg.results.EffectResult;

public class CostResolution {
    private final EffectResult[] _effectResults;
    private final boolean _successful;

    public CostResolution(EffectResult[] effectResults, boolean successful) {
        _effectResults = effectResults;
        _successful = successful;
    }

    public EffectResult[] getEffectResults() {
        return _effectResults;
    }

    public boolean isSuccessful() {
        return _successful;
    }
}
