package com.gempukku.lotro.cards;

import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.game.DefaultGame;

public interface AidCostSource {
    boolean canPayAidCost(DefaultActionContext<DefaultGame> actionContext);

    void appendAidCost(CostToEffectAction action, DefaultActionContext<DefaultGame> actionContext);
}
