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
    public List<? extends Action> getRequiredBeforeTriggers(Effect effect) { return null; }

    @Override
    public List<? extends Action> getRequiredAfterTriggers(EffectResult effectResult) {
        return null;
    }

    @Override
    public List<? extends Action> getOptionalAfterTriggerActions(String playerId, EffectResult effectResult) {
        return null;
    }

    @Override
    public List<? extends Action> getOptionalBeforeTriggers(String playerId, Effect effect) {
        return null;
    }
}
