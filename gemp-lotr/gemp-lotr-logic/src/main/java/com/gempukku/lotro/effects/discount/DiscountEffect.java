package com.gempukku.lotro.effects.discount;

import com.gempukku.lotro.effects.Effect;
import com.gempukku.lotro.game.DefaultGame;

public interface DiscountEffect extends Effect<DefaultGame> {
    int getMaximumPossibleDiscount(DefaultGame game);

    void setMinimalRequiredDiscount(int minimalDiscount);

    int getDiscountPaidFor();
}
