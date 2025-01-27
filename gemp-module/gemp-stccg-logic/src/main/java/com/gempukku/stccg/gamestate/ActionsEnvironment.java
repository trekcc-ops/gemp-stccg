package com.gempukku.stccg.gamestate;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;

import java.util.*;

@JsonIncludeProperties({"actions"})
public interface ActionsEnvironment {

    List<TopLevelSelectableAction> getRequiredAfterTriggers(Collection<? extends ActionResult> effectResults);

    Map<TopLevelSelectableAction, ActionResult> getOptionalAfterTriggers(DefaultGame cardGame, String playerId,
                                                       Collection<? extends ActionResult> effectResults);

    List<TopLevelSelectableAction> getOptionalAfterActions(DefaultGame cardGame, String playerId,
                                                           Collection<? extends ActionResult> effectResults);

    void addUntilEndOfTurnActionProxy(ActionProxy actionProxy);

    List<TopLevelSelectableAction> getPhaseActions(DefaultGame cardGame, Player player);

    void addActionToStack(Action action) throws InvalidGameLogicException;

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