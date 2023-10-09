package com.gempukku.stccg.cards;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.effects.discount.DiscountEffect;
import com.gempukku.stccg.game.DefaultGame;

public interface DiscountSource {
    int getPotentialDiscount(DefaultActionContext<DefaultGame> actionContext);

    DiscountEffect getDiscountEffect(CostToEffectAction action, DefaultActionContext actionContext);
}
