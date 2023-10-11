package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.ActionContext;

public interface ActionSource {
    boolean requiresRanger();

    boolean isValid(ActionContext actionContext);

    void createAction(CostToEffectAction action, ActionContext actionContext);
}
