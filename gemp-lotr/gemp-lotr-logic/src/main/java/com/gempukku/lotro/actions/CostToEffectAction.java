package com.gempukku.lotro.actions;

import com.gempukku.lotro.effects.discount.DiscountEffect;
import com.gempukku.lotro.actions.Action;
import com.gempukku.lotro.effects.Effect;

public interface CostToEffectAction extends Action {
    void appendPotentialDiscount(DiscountEffect cost);

    /**
     * Inserts the specified costs as the next costs to be executed.
     *
     * @param cost
     */
    void insertCost(Effect... cost);

    /**
     * Appends the specified cost to the list of the costs. It will be executed after all the other costs currently in
     * the queue.
     *
     * @param cost
     */
    void appendCost(Effect cost);

    /**
     * Inserts the specified effects as the next effects to be executedD.
     *
     * @param effect
     */
    void insertEffect(Effect... effect);

    /**
     * Appends the specified effect to the list of the effects. It will be executed after all the other effects currently
     * in the queue.
     *
     * @param effect
     */
    void appendEffect(Effect effect);

    void setText(String text);

    void setPaidToil(boolean toilPaid);

    boolean wasCarriedOut();
}
