package com.gempukku.stccg.cards.blueprints.effect;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.AppendableAction;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.ActionContext;

public interface EffectBlueprint {
    void addEffectToAction(boolean cost, AppendableAction action, ActionContext actionContext);
    boolean isPlayableInFull(ActionContext actionContext);
    default boolean isPlayabilityCheckedForEffect() {
        return false;
    }
}