package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.ActionContext;

public interface EffectAppender<AbstractContext extends ActionContext> {
    void appendEffect(boolean cost, CostToEffectAction action, AbstractContext actionContext);
    boolean isPlayableInFull(AbstractContext actionContext);
    default boolean isPlayabilityCheckedForEffect() {
        return false;
    }
}
