package com.gempukku.stccg.cards;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.game.DefaultGame;

public interface AidCostSource {
    boolean canPayAidCost(ActionContext actionContext);

    void appendAidCost(CostToEffectAction action, ActionContext actionContext);
}
