package com.gempukku.stccg.cards.blueprints.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.ActionContext;

public interface EffectAppender {
    void appendEffect(boolean cost, CostToEffectAction action, ActionContext actionContext);
    boolean isPlayableInFull(ActionContext actionContext);
    default boolean isPlayabilityCheckedForEffect() {
        return false;
    }
}
