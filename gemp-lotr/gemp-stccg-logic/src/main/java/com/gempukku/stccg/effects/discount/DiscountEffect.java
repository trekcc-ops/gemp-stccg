package com.gempukku.stccg.effects.discount;

import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.game.DefaultGame;

public interface DiscountEffect extends Effect<DefaultGame> {
    int getMaximumPossibleDiscount(DefaultGame game);

    void setMinimalRequiredDiscount(int minimalDiscount);

    int getDiscountPaidFor();
}
