package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;


public class ActivateCardResult extends EffectResult {
    private boolean _effectCancelled;

    public ActivateCardResult(ActivateCardEffect effect) {
        super(Type.ACTIVATE, effect);
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
