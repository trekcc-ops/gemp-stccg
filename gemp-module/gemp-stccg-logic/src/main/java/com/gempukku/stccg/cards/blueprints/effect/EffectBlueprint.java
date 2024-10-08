package com.gempukku.stccg.cards.blueprints.effect;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.ActionContext;

public interface EffectBlueprint {
    void addEffectToAction(boolean cost, CostToEffectAction action, ActionContext actionContext);
    boolean isPlayableInFull(ActionContext actionContext);
    default boolean isPlayabilityCheckedForEffect() {
        return false;
    }
}