package com.gempukku.stccg.gamestate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.game.*;

import java.util.*;

public class DefaultActionsEnvironment implements ActionsEnvironment {

    @JsonProperty("actions")
    private final Map<Integer, Action> _createdActionMap = new HashMap<>();
    private final Stack<Action> _actionStack = new Stack<>();
    private final List<ActionProxy> _actionProxies = new LinkedList<>();
    private final List<ActionProxy> _untilEndOfTurnActionProxies = new LinkedList<>();
    private final List<Action> _performedActions = new LinkedList<>();
    private Set<ActionResult> _actionResults = new HashSet<>();
    private int _nextActionId = 1;

    @Override
    public void emitEffectResult(ActionResult actionResult) {
        _actionResults.add(actionResult);
    }

    public Set<ActionResult> consumeEffectResults() {
        Set<ActionResult> result = _actionResults;
        _actionResults = new HashSet<>();
        return result;
    }

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

    @Override
    public List<TopLevelSelectableAction> getRequiredAfterTriggers(Collection<? extends ActionResult> effectResults) {
        List<TopLevelSelectableAction> gatheredActions = new LinkedList<>();

        if (effectResults != null) {
            for (ActionProxy actionProxy : _actionProxies) {
                for (ActionResult actionResult : effectResults) {
                    List<TopLevelSelectableAction> actions = actionProxy.getRequiredAfterTriggers(actionResult);
                    if (actions != null)
                        gatheredActions.addAll(actions);
                }
            }
        }

        return gatheredActions;
    }

    @Override
    public Map<TopLevelSelectableAction, ActionResult> getOptionalAfterTriggers(DefaultGame cardGame, String playerId,
                                                              Collection<? extends ActionResult> effectResults) {
        final Map<TopLevelSelectableAction, ActionResult> gatheredActions = new HashMap<>();

        try {
            if (effectResults != null) {
                for (ActionResult actionResult : effectResults) {
                    List<TopLevelSelectableAction> actions = actionResult.getOptionalAfterTriggerActions(
                            cardGame.getGameState().getPlayer(playerId));
                    if (actions != null) {
                        for (TopLevelSelectableAction action : actions) {
                            if (!actionResult.wasOptionalTriggerUsed(action)) {
                                gatheredActions.put(action, actionResult);
                            }
                        }
                    }
                }
            }
        } catch(PlayerNotFoundException exp) {
            cardGame.sendErrorMessage(exp);
        }

        return gatheredActions;
    }

    @Override
    public List<TopLevelSelectableAction> getOptionalAfterActions(DefaultGame cardGame, String playerId,
                                                                  Collection<? extends ActionResult> effectResults) {
        List<TopLevelSelectableAction> result = new LinkedList<>();

        if (effectResults != null) {
            for (ActionProxy actionProxy : _actionProxies) {
                for (ActionResult actionResult : effectResults) {
                    List<TopLevelSelectableAction> actions =
                            actionProxy.getOptionalAfterActions(playerId, actionResult);
                    List<TopLevelSelectableAction> playableActions = getPlayableActions(cardGame, playerId, actions);
                    result.addAll(playableActions);
                }
            }
        }

        return result;
    }


    private <T extends Action> List<T> getPlayableActions(DefaultGame cardGame, String playerId, Iterable<T> actions) {
        List<T> result = new LinkedList<>();
        if (actions != null) {
            for (T action : actions) {
                if (cardGame.getModifiersQuerying().canPerformAction(playerId, action) &&
                        action.canBeInitiated(cardGame))
                    result.add(action);
            }
        }
        return result;
    }


    @Override
    public List<TopLevelSelectableAction> getPhaseActions(DefaultGame cardGame, Player player) {
        List<TopLevelSelectableAction> result = new LinkedList<>();

        for (ActionProxy actionProxy : _actionProxies) {
            List<TopLevelSelectableAction> actions = actionProxy.getPhaseActions(player);
            List<TopLevelSelectableAction> playableActions =
                    getPlayableActions(cardGame, player.getPlayerId(), actions);
            result.addAll(playableActions);
        }

        return result;
    }


    @Override
    public void addActionToStack(Action action) throws InvalidGameLogicException {
        action.startPerforming(); // Set action status
        _actionStack.add(action);
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

}