package com.gempukku.stccg.effects;

public interface DiscountEffect extends Effect {
    int getMaximumPossibleDiscount();

    void setMinimalRequiredDiscount(int minimalDiscount);

    int getDiscountPaidFor();
}
