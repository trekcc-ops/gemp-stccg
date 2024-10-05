package com.gempukku.stccg.actions;

import com.gempukku.stccg.actions.Effect;

public interface DiscountEffect extends Effect {
    int getMaximumPossibleDiscount();

    int getDiscountPaidFor();
}
