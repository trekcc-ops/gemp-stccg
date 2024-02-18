package com.gempukku.stccg.actions;

import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.results.EffectResult;

import java.util.List;

public abstract class AbstractActionProxy implements ActionProxy {
    @Override
    public List<? extends Action> getPhaseActions(String playerId) {
        return null;
    }

    @Override
    public List<? extends Action> getOptionalBeforeActions(String playerId, Effect effect) { return null; }

    @Override
    public List<? extends Action> getOptionalAfterActions(String playerId, EffectResult effectResult) {
        return null;
    }

    @Override
    public List<? extends RequiredTriggerAction> getRequiredBeforeTriggers(Effect effect) { return null; }

    @Override
    public List<? extends RequiredTriggerAction> getRequiredAfterTriggers(EffectResult effectResult) {
        return null;
    }

    @Override
    public List<? extends OptionalTriggerAction> getOptionalAfterTriggerActions(String playerId, EffectResult effectResult) {
        return null;
    }

    @Override
    public List<? extends OptionalTriggerAction> getOptionalBeforeTriggers(String playerId, Effect effect) {
        return null;
    }
}
