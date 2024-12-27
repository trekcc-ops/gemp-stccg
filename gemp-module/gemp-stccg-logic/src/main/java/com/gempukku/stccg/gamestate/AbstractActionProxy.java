package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.EffectResult;

import java.util.List;

public abstract class AbstractActionProxy implements ActionProxy {
    @Override
    public List<? extends Action> getPhaseActions(String playerId) {
        return null;
    }

    @Override
    public List<? extends Action> getOptionalAfterActions(String playerId, EffectResult effectResult) {
        return null;
    }

    @Override
    public List<? extends Action> getRequiredAfterTriggers(EffectResult effectResult) {
        return null;
    }

}