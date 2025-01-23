package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.game.DefaultGame;

import java.util.*;

public interface ActionsEnvironment {

    List<TopLevelSelectableAction> getRequiredAfterTriggers(Collection<? extends ActionResult> effectResults);

    Map<TopLevelSelectableAction, ActionResult> getOptionalAfterTriggers(DefaultGame cardGame, String playerId,
                                                       Collection<? extends ActionResult> effectResults);

    List<TopLevelSelectableAction> getOptionalAfterActions(String playerId, Collection<? extends ActionResult> effectResults);

    List<TopLevelSelectableAction> getPhaseActions(String playerId);

    void addUntilEndOfTurnActionProxy(ActionProxy actionProxy);

    void addActionToStack(Action action);

    void emitEffectResult(ActionResult actionResult);

    Set<ActionResult> consumeEffectResults();
    void signalEndOfTurn();
    void addAlwaysOnActionProxy(ActionProxy actionProxy);

    Stack<Action> getActionStack();

    List<Action> getPerformedActions();

    boolean hasNoActionsInProgress();

    void removeCompletedActionFromStack(Action action);

    Action getCurrentAction();

    int getNextActionId();

    void incrementActionId();

    Action getActionById(int actionId);

    void logAction(Action action);
}