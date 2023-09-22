package com.gempukku.lotro.actions;

import com.gempukku.lotro.actioncontext.DefaultActionContext;
import com.gempukku.lotro.game.DefaultGame;

public interface ActionSource {
    boolean requiresRanger();

    boolean isValid(DefaultActionContext<DefaultGame> actionContext);

    void createAction(CostToEffectAction action, DefaultActionContext<DefaultGame> actionContext);
}
