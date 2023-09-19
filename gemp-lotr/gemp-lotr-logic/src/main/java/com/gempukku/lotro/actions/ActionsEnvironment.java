package com.gempukku.lotro.actions;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.effects.Effect;
import com.gempukku.lotro.effects.EffectResult;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ActionsEnvironment {
    List<Action> getRequiredBeforeTriggers(Effect effect);

    List<Action> getOptionalBeforeTriggers(String playerId, Effect effect);

    List<Action> getOptionalBeforeActions(String playerId, Effect effect);

    List<Action> getRequiredAfterTriggers(Collection<? extends EffectResult> effectResults);

    Map<OptionalTriggerAction, EffectResult> getOptionalAfterTriggers(String playerId, Collection<? extends EffectResult> effectResults);

    List<Action> getOptionalAfterActions(String playerId, Collection<? extends EffectResult> effectResults);

    List<Action> getPhaseActions(String playerId);

    void addUntilStartOfPhaseActionProxy(ActionProxy actionProxy, Phase phase);

    void addUntilEndOfPhaseActionProxy(ActionProxy actionProxy, Phase phase);

    void addUntilEndOfTurnActionProxy(ActionProxy actionProxy);

    void addActionToStack(Action action);

    void emitEffectResult(EffectResult effectResult);

    List<EffectResult> getTurnEffectResults();

    List<EffectResult> getPhaseEffectResults();
}