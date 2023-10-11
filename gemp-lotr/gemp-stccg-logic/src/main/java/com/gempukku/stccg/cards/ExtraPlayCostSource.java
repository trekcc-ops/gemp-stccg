package com.gempukku.stccg.cards;

import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.ExtraPlayCost;

public interface ExtraPlayCostSource {
    ExtraPlayCost getExtraPlayCost(ActionContext actionContext);
}
