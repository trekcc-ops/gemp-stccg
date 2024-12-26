package com.gempukku.stccg.actions;

public interface CostToEffectAction extends Action {

    void setText(String text);

    boolean wasCarriedOut();

    String getCardActionPrefix();
}