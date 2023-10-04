package com.gempukku.lotro.actions;

import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.effects.Effect;
import com.gempukku.lotro.effects.EffectResult;

import java.util.List;

public abstract class AbstractActionProxy<AbstractGame extends DefaultGame> implements ActionProxy<AbstractGame> {
    @Override
    public List<? extends Action> getPhaseActions(String playerId, AbstractGame game) {
        return null;
    }

    @Override
    public List<? extends Action> getOptionalBeforeActions(String playerId, AbstractGame game, Effect effect) {
        return null;
    }

    @Override
    public List<? extends Action> getOptionalAfterActions(String playerId, AbstractGame game, EffectResult effectResult) {
        return null;
    }

    @Override
    public List<? extends RequiredTriggerAction> getRequiredBeforeTriggers(AbstractGame game, Effect effect) {
        return null;
    }

    @Override
    public List<? extends RequiredTriggerAction> getRequiredAfterTriggers(AbstractGame game, EffectResult effectResult) {
        return null;
    }

    @Override
    public List<? extends OptionalTriggerAction> getOptionalAfterTriggerActions(String playerId, AbstractGame game, EffectResult effectResult) {
        return null;
    }

    @Override
    public List<? extends OptionalTriggerAction> getOptionalBeforeTriggers(String playerId, AbstractGame game, Effect effect) {
        return null;
    }
}
