package com.gempukku.stccg.gamestate;

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

public class DefaultActionsEnvironment implements ActionsEnvironment {

    private final Map<Integer, Action> _createdActionMap = new HashMap<>();
    private final Stack<Action> _actionStack = new Stack<>();
    private final List<ActionProxy> _actionProxies = new LinkedList<>();
    private final List<ActionProxy> _untilEndOfTurnActionProxies = new LinkedList<>();
    private final List<Action> _performedActions = new LinkedList<>();
    private int _nextActionId = 1;

    public void addAlwaysOnActionProxy(ActionProxy actionProxy) {
        _actionProxies.add(actionProxy);
    }

    public void signalEndOfTurn() {
        _actionProxies.removeAll(_untilEndOfTurnActionProxies);
        _untilEndOfTurnActionProxies.clear();
    }

    @Override
    public void addUntilEndOfTurnActionProxy(ActionProxy actionProxy) {
        _actionProxies.add(actionProxy);
        _untilEndOfTurnActionProxies.add(actionProxy);
    }

    public List<TopLevelSelectableAction> getRequiredAfterTriggers(DefaultGame cardGame, ActionResult actionResult) {
        List<TopLevelSelectableAction> gatheredActions = new LinkedList<>();

        if (actionResult != null) {
            for (ActionProxy actionProxy : _actionProxies) {
                List<TopLevelSelectableAction> actions = actionProxy.getRequiredAfterTriggers(cardGame, actionResult);
                if (actions != null)
                    gatheredActions.addAll(actions);
            }
        }

        return gatheredActions;
    }


    @Override
    public Map<TopLevelSelectableAction, ActionResult> getOptionalAfterTriggers(DefaultGame cardGame, Player player,
                                                              Collection<? extends ActionResult> effectResults) {
        final Map<TopLevelSelectableAction, ActionResult> gatheredActions = new HashMap<>();

        if (effectResults != null) {
            for (ActionResult actionResult : effectResults) {
                List<TopLevelSelectableAction> actions = actionResult.getOptionalAfterTriggerActions(cardGame, player);
                if (actions != null) {
                    for (TopLevelSelectableAction action : actions) {
                        if (!actionResult.wasOptionalTriggerUsed(action)) {
                            gatheredActions.put(action, actionResult);
                        }
                    }
                }
            }
        }

        return gatheredActions;
    }

    @Override
    public List<TopLevelSelectableAction> getOptionalAfterActions(DefaultGame cardGame, Player player,
                                                                  Collection<? extends ActionResult> effectResults) {
        List<TopLevelSelectableAction> result = new LinkedList<>();

        if (effectResults != null) {
            for (ActionProxy actionProxy : _actionProxies) {
                for (ActionResult actionResult : effectResults) {
                    List<TopLevelSelectableAction> actions =
                            actionProxy.getOptionalAfterActions(cardGame, player.getPlayerId(), actionResult);
                    List<TopLevelSelectableAction> playableActions = getPlayableActions(cardGame, player, actions);
                    result.addAll(playableActions);
                }
            }
        }

        return result;
    }


    private <T extends Action> List<T> getPlayableActions(DefaultGame cardGame, Player player, Iterable<T> actions) {
        List<T> result = new LinkedList<>();
        if (actions != null) {
            for (T action : actions) {
                if (cardGame.canPerformAction(player.getPlayerId(), action) && action.canBeInitiated(cardGame)) {
                    result.add(action);
                }
            }
        }
        return result;
    }


    @Override
    public List<TopLevelSelectableAction> getPhaseActions(DefaultGame cardGame, Player player) {
        List<TopLevelSelectableAction> result = new LinkedList<>();

        for (ActionProxy actionProxy : _actionProxies) {
            List<TopLevelSelectableAction> actions = actionProxy.getPhaseActions(cardGame, player);
            List<TopLevelSelectableAction> playableActions = getPlayableActions(cardGame, player, actions);
            result.addAll(playableActions);
        }

        return result;
    }


    @Override
    public void addActionToStack(Action action) throws InvalidGameLogicException {
        if (action != null) {
            action.startPerforming(); // Set action status
            _actionStack.add(action);
        }
    }

    public Stack<Action> getActionStack() { return _actionStack; }

    @Override
    public List<Action> getPerformedActions() {
        return _performedActions;
    }

    @Override
    public boolean hasNoActionsInProgress() {
        return _actionStack.isEmpty();
    }

    @Override
    public void removeCompletedActionFromStack(Action action) throws ActionOrderOfOperationException {
        if (!action.isInProgress()) {
            _actionStack.remove(action);
            _performedActions.add(action);
        } else {
            throw new ActionOrderOfOperationException("Tried to remove incomplete action from stack of class " +
                    action.getClass().getSimpleName());
        }
    }

    @Override
    public Action getCurrentAction() {
        return _actionStack.peek();
    }

    @Override
    public int getNextActionId() {
        return _nextActionId;
    }

    @Override
    public void incrementActionId() {
        _nextActionId++;
    }

    public Action getActionById(int actionId) {
        return _createdActionMap.get(actionId);
    }

    public void logAction(Action action) {
        _createdActionMap.put(action.getActionId(), action);
    }

    public void logCompletedActionNotInStack(Action action) {
        logAction(action);
        _performedActions.add(action);
    }

    public Map<Integer, Action> getAllActions() {
        return _createdActionMap;
    }

    public void executeNextSubAction(DefaultGame cardGame) throws InvalidGameOperationException,
            PlayerNotFoundException, InvalidGameLogicException, CardNotFoundException {
        Action currentAction = getCurrentAction();
        ActionResult actionResult = currentAction.getResult();

        if (actionResult != null) {
            actionResult.initialize(cardGame); // won't do anything if already initialized
            actionResult.addNextActionToStack(cardGame, currentAction);
        } else {
            if (currentAction.isInProgress()) {
                Action nextAction = currentAction.nextAction(cardGame);
                addActionToStack(nextAction); // won't do anything if nextAction is null
            } else if (!currentAction.isInProgress() && currentAction.getResult() == null) {
                removeCompletedActionFromStack(currentAction);
                cardGame.sendActionResultToClient();
            } else if (cardGame.isCarryingOutEffects() && currentAction.getResult() == null) {
                throw new InvalidGameLogicException("Unable to process action");
            }
        }
    }

    public void carryOutPendingActions(DefaultGame cardGame) throws PlayerNotFoundException,
            InvalidGameOperationException, InvalidGameLogicException, CardNotFoundException {
        if (hasNoActionsInProgress())
            try {
                cardGame.continueCurrentProcess();
                cardGame.sendActionResultToClient();
            } catch(InvalidGameLogicException exp) {
                cardGame.sendErrorMessage(exp);
            }
        else {
            executeNextSubAction(cardGame);
        }
    }
}