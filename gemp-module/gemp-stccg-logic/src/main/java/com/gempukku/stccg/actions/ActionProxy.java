package com.gempukku.stccg.actions;

import java.util.List;

public interface ActionProxy {
    List<? extends Action> getPhaseActions(String playerId);

    List<? extends Action> getOptionalBeforeActions(String playerId, Effect effect);

    List<? extends Action> getRequiredBeforeTriggers(Effect effect);

    List<? extends Action> getOptionalBeforeTriggerActions(String playerId, Effect effect);

    List<? extends Action> getOptionalAfterActions(String playerId, EffectResult effectResult);

    List<? extends Action> getRequiredAfterTriggers(EffectResult effectResult);

}
