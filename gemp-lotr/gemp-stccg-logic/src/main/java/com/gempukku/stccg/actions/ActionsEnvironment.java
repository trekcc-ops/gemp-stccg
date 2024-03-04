package com.gempukku.stccg.actions;

import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Snapshotable;

import java.util.*;

public interface ActionsEnvironment extends Snapshotable<ActionsEnvironment> {
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

    Set<EffectResult> consumeEffectResults();
    void signalEndOfTurn();
    void addAlwaysOnActionProxy(ActionProxy actionProxy);
    DefaultGame getGame();
    Stack<Action> getActionStack();

}