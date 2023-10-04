package com.gempukku.lotro.cards;

import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.effects.discount.DiscountEffect;
import com.gempukku.lotro.game.DefaultGame;

public interface DiscountSource {
    int getPotentialDiscount(DefaultActionContext<DefaultGame> actionContext);

    DiscountEffect getDiscountEffect(CostToEffectAction action, DefaultActionContext actionContext);
}
