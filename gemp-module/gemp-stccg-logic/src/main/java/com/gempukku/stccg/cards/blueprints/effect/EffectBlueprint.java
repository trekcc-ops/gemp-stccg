package com.gempukku.stccg.cards.blueprints.effect;

import com.gempukku.stccg.actions.CardPerformedAction;
import com.gempukku.stccg.cards.ActionContext;

public interface EffectBlueprint {
    void addEffectToAction(boolean cost, CardPerformedAction action, ActionContext actionContext);
    boolean isPlayableInFull(ActionContext actionContext);
    default boolean isPlayabilityCheckedForEffect() {
        return false;
    }
}