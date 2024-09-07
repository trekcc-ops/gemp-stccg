package com.gempukku.stccg.cards;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

public interface TwilightCostModifierSource {
    int getTwilightCostModifier(ActionContext actionContext, PhysicalCard target);
}
