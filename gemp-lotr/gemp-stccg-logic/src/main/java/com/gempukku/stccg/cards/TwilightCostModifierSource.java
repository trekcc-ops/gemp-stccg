package com.gempukku.stccg.cards;

import com.gempukku.stccg.game.DefaultGame;

public interface TwilightCostModifierSource {
    int getTwilightCostModifier(ActionContext actionContext, PhysicalCard target);
}
