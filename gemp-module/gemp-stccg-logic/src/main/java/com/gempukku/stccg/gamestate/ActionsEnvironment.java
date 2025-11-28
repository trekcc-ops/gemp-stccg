package com.gempukku.stccg.gamestate;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.game.ActionOrderOfOperationException;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.*;

@JsonIncludeProperties({"actions"})
public interface ActionsEnvironment {

    List<TopLevelSelectableAction> getRequiredAfterTriggers(DefaultGame cardGame, ActionResult actionResult);

    Map<TopLevelSelectableAction, ActionResult> getOptionalAfterTriggers(DefaultGame cardGame, Player player,
                                                       Collection<? extends ActionResult> effectResults);

    List<TopLevelSelectableAction> getOptionalAfterActions(DefaultGame cardGame, Player player,
                                                           Collection<? extends ActionResult> effectResults);

    void addUntilEndOfTurnActionProxy(ActionProxy actionProxy);

    List<TopLevelSelectableAction> getPhaseActions(DefaultGame cardGame, Player player);

    void addActionToStack(Action action) throws InvalidGameLogicException;

    void signalEndOfTurn();
    void addAlwaysOnActionProxy(ActionProxy actionProxy);

    Stack<Action> getActionStack();

    List<Action> getPerformedActions();

    boolean hasNoActionsInProgress();

    void removeCompletedActionFromStack(Action action) throws ActionOrderOfOperationException;

    Action getCurrentAction();

    int getNextActionId();

    void incrementActionId();

    Action getActionById(int actionId);

    void logAction(Action action);

    Map<Integer, Action> getAllActions();

    void logCompletedActionNotInStack(Action action);

    void executeNextSubAction(DefaultGame cardGame) throws InvalidGameOperationException,
            PlayerNotFoundException, InvalidGameLogicException, CardNotFoundException;

    void carryOutPendingActions(DefaultGame cardGame) throws InvalidGameOperationException,
            PlayerNotFoundException, InvalidGameLogicException, CardNotFoundException;
}