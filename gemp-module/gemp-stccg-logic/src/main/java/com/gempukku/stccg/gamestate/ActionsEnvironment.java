package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.EffectResult;
import com.gempukku.stccg.game.DefaultGame;

import java.util.*;

public interface ActionsEnvironment {

    List<Action> getRequiredAfterTriggers(Collection<? extends EffectResult> effectResults);

    Map<Action, EffectResult> getOptionalAfterTriggers(String playerId,
                                                       Collection<? extends EffectResult> effectResults);

    List<Action> getOptionalAfterActions(String playerId, Collection<? extends EffectResult> effectResults);

    List<Action> getPhaseActions(String playerId);

    void addUntilEndOfTurnActionProxy(ActionProxy actionProxy);

    void addActionToStack(Action action);

    void emitEffectResult(EffectResult effectResult);

    Set<EffectResult> consumeEffectResults();
    void signalEndOfTurn();
    void addAlwaysOnActionProxy(ActionProxy actionProxy);
    DefaultGame getGame();
    Stack<Action> getActionStack();

    List<Action> getPerformedActions();

    boolean hasNoActionsInProgress();

    void removeCompletedAction(Action action);

    Action getCurrentAction();

    int getNextActionId();

    void incrementActionId();
}