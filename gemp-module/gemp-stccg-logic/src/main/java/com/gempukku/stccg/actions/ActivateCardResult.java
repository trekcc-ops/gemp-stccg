package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;


public class ActivateCardResult extends EffectResult {

    public ActivateCardResult(ActivateCardEffect effect) {
        super(Type.ACTIVATE, effect);
    }

    public PhysicalCard getSource() {
        return _source;
    }

    public boolean isEffectCancelled() {
        return false;
    }
}