package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.DefaultActionContext;
import com.gempukku.stccg.game.DefaultGame;

public interface ActionSource {
    boolean requiresRanger();

    boolean isValid(DefaultActionContext<DefaultGame> actionContext);

    void createAction(CostToEffectAction action, DefaultActionContext<DefaultGame> actionContext);
}
