package com.gempukku.stccg.actions;

public interface CostToEffectAction extends Action {

    /**
     * Inserts the specified costs as the next costs to be executed.
     *
     * @param cost
     */
    void insertCost(Effect cost);

    /**
     * Appends the specified cost to the list of the costs. It will be executed after all the other costs currently in
     * the queue.
     *
     * @param cost
     */
    void appendCost(Effect cost);

    /**
     * Appends the specified effect to the list of the effects. It will be executed after all the other effects currently
     * in the queue.
     *
     * @param effect
     */
    void appendEffect(Effect effect);

    void setText(String text);

    boolean wasCarriedOut();

    String getCardActionPrefix();
}