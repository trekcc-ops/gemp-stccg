package com.gempukku.stccg.effects;

import com.gempukku.stccg.game.DefaultGame;

public interface DiscountEffect extends Effect {
    int getMaximumPossibleDiscount(DefaultGame game);

    void setMinimalRequiredDiscount(int minimalDiscount);

    int getDiscountPaidFor();
}
