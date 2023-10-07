package com.gempukku.lotro.results;

import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.effects.EffectResult;

public class ActivateCardResult extends EffectResult {
    private final PhysicalCard _source;
    private boolean _effectCancelled;

    public ActivateCardResult(PhysicalCard source) {
        super(Type.ACTIVATE);
        _source = source;
    }

    public PhysicalCard getSource() {
        return _source;
    }

    public void cancelEffect() {
        _effectCancelled = true;
    }

    public boolean isEffectCancelled() {
        return _effectCancelled;
    }
}
