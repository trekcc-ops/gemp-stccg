package com.gempukku.stccg.actions;

public interface CostToEffectAction extends Action {

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