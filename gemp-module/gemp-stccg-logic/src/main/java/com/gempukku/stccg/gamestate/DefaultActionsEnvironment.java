package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

import java.util.*;

public class DefaultActionsEnvironment implements ActionsEnvironment {
    private final DefaultGame _game;
    private final Map<Integer, Action> _createdActionMap = new HashMap<>();
    private final Stack<Action> _actionStack;
    private final List<ActionProxy> _actionProxies = new LinkedList<>();
    private final List<ActionProxy> _untilEndOfTurnActionProxies = new LinkedList<>();
    private final List<Action> _performedActions = new LinkedList<>();
    private Set<ActionResult> _actionResults = new HashSet<>();
    private int _nextActionId = 1;

    public DefaultActionsEnvironment(DefaultGame game) {
        this(game, new Stack<>());
    }

    public DefaultActionsEnvironment(DefaultGame game, Stack<Action> actionStack) {
        _game = game;
        _actionStack = actionStack;
    }

    private DefaultActionsEnvironment(DefaultGame game, Stack<Action> actionStack,
                                      Collection<? extends ActionProxy> actionProxies,
                                      Collection<? extends ActionProxy> untilEndOfTurnActionProxies,
                                      Iterable<? extends Action> performedActions) {
        _game = game;
        _actionStack = actionStack;
        _actionProxies.addAll(actionProxies);
        _untilEndOfTurnActionProxies.addAll(untilEndOfTurnActionProxies);
        for (Action action : performedActions) {
            _performedActions.add(action);
        }
    }

    public DefaultGame getGame() { return _game; }

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
    public Map<TopLevelSelectableAction, ActionResult> getOptionalAfterTriggers(String playerId,
                                                              Collection<? extends ActionResult> effectResults) {
        final Map<TopLevelSelectableAction, ActionResult> gatheredActions = new HashMap<>();

        if (effectResults != null) {
            for (ActionResult actionResult : effectResults) {
                List<TopLevelSelectableAction> actions = actionResult.getOptionalAfterTriggerActions(
                        _game.getGameState().getPlayer(playerId));
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
    public List<TopLevelSelectableAction> getOptionalAfterActions(String playerId,
                                                                  Collection<? extends ActionResult> effectResults) {
        List<TopLevelSelectableAction> result = new LinkedList<>();

        if (effectResults != null) {
            for (ActionProxy actionProxy : _actionProxies) {
                for (ActionResult actionResult : effectResults) {
                    List<TopLevelSelectableAction> actions =
                            actionProxy.getOptionalAfterActions(playerId, actionResult);
                    List<TopLevelSelectableAction> playableActions = getPlayableActions(playerId, actions);
                    result.addAll(playableActions);
                }
            }
        }

        return result;
    }

    private <T extends Action> List<T> getPlayableActions(String playerId, Iterable<T> actions) {
        List<T> result = new LinkedList<>();
        if (actions != null) {
            for (T action : actions) {
                if (_game.getModifiersQuerying().canPerformAction(playerId, action) && action.canBeInitiated(_game))
                    result.add(action);
            }
        }
        return result;
    }

    @Override
    public List<TopLevelSelectableAction> getPhaseActions(String playerId) {
        List<TopLevelSelectableAction> result = new LinkedList<>();

        for (ActionProxy actionProxy : _actionProxies) {
            List<TopLevelSelectableAction> actions = actionProxy.getPhaseActions(playerId);
            List<TopLevelSelectableAction> playableActions = getPlayableActions(playerId, actions);
            result.addAll(playableActions);
        }

        return result;
    }

    @Override
    public void addActionToStack(Action action) {
        try {
            action.startPerforming(); // Set action status
            _actionStack.add(action);
        } catch(InvalidGameLogicException exp) {
            _game.sendErrorMessage(exp);
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
    public void removeCompletedActionFromStack(Action action) {
        _actionStack.remove(action);
        _performedActions.add(action);
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