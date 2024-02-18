package com.gempukku.stccg.actions;

import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.results.EffectResult;

import java.util.List;

public interface ActionProxy {
    List<? extends Action> getPhaseActions(String playerId);

    List<? extends Action> getOptionalBeforeActions(String playerId, Effect effect);

    List<? extends RequiredTriggerAction> getRequiredBeforeTriggers(Effect effect);

    List<? extends OptionalTriggerAction> getOptionalBeforeTriggers(String playerId, Effect effect);

    List<? extends Action> getOptionalAfterActions(String playerId, EffectResult effectResult);

    List<? extends RequiredTriggerAction> getRequiredAfterTriggers(EffectResult effectResult);

    List<? extends OptionalTriggerAction> getOptionalAfterTriggerActions(String playerId, EffectResult effectResult);
}
