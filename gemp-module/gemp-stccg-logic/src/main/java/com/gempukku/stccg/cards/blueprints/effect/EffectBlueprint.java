package com.gempukku.stccg.cards.blueprints.effect;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.cards.ActionContext;

public interface EffectBlueprint {
    void addEffectToAction(boolean cost, Action action, ActionContext actionContext);
    boolean isPlayableInFull(ActionContext actionContext);
    default boolean isPlayabilityCheckedForEffect() {
        return false;
    }
}