package com.gempukku.stccg.cards;

public interface TwilightCostModifierSource {
    int getTwilightCostModifier(ActionContext actionContext, PhysicalCard target);
}
