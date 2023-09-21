package com.gempukku.lotro.cards;

import com.gempukku.lotro.actioncontext.DefaultActionContext;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.modifiers.ExtraPlayCost;

public interface ExtraPlayCostSource {
    ExtraPlayCost getExtraPlayCost(DefaultActionContext<DefaultGame> actionContext);
}
