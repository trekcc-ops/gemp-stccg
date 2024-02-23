package com.gempukku.stccg.actions;

import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.results.EffectResult;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ActionsEnvironment {
    List<Action> getRequiredBeforeTriggers(Effect effect);

    List<Action> getOptionalBeforeTriggers(String playerId, Effect effect);

    List<Action> getOptionalBeforeActions(String playerId, Effect effect);

    List<Action> getRequiredAfterTriggers(Collection<? extends EffectResult> effectResults);

    Map<Action, EffectResult> getOptionalAfterTriggers(String playerId, Collection<? extends EffectResult> effectResults);

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