package com.gempukku.stccg.cards;

import com.gempukku.stccg.game.DefaultGame;

public interface TwilightCostModifierSource {
    int getTwilightCostModifier(DefaultActionContext<DefaultGame> actionContext, PhysicalCard target);
}
