package com.gempukku.lotro.cards;

import com.gempukku.lotro.actioncontext.DefaultActionContext;
import com.gempukku.lotro.game.DefaultGame;

public interface TwilightCostModifierSource {
    int getTwilightCostModifier(DefaultActionContext<DefaultGame> actionContext, LotroPhysicalCard target);
}
