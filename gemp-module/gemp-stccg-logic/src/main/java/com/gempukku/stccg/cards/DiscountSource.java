package com.gempukku.stccg.cards;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.DiscountEffect;

public interface DiscountSource {
    int getPotentialDiscount(ActionContext actionContext);

    DiscountEffect getDiscountEffect(CostToEffectAction action, ActionContext actionContext);
}
