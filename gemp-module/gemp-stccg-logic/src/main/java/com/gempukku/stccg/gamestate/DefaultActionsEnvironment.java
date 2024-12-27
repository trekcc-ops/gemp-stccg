package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.game.DefaultGame;

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
    public List<Action> getRequiredAfterTriggers(Collection<? extends ActionResult> effectResults) {
        List<Action> gatheredActions = new LinkedList<>();

        if (effectResults != null) {
            for (ActionProxy actionProxy : _actionProxies) {
                for (ActionResult actionResult : effectResults) {
                    List<? extends Action> actions = actionProxy.getRequiredAfterTriggers(actionResult);
                    if (actions != null)
                        gatheredActions.addAll(actions);
                }
            }
        }

        return gatheredActions;
    }

    @Override
    public Map<Action, ActionResult> getOptionalAfterTriggers(String playerId,
                                                              Collection<? extends ActionResult> effectResults) {
        final Map<Action, ActionResult> gatheredActions = new HashMap<>();

        if (effectResults != null) {
            for (ActionResult actionResult : effectResults) {
                List<? extends Action> actions = actionResult.getOptionalAfterTriggerActions(
                        _game.getGameState().getPlayer(playerId));
                if (actions != null) {
                    for (Action action : actions) {
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
    public List<Action> getOptionalAfterActions(String playerId, Collection<? extends ActionResult> effectResults) {
        List<Action> result = new LinkedList<>();

        if (effectResults != null) {
            for (ActionProxy actionProxy : _actionProxies) {
                for (ActionResult actionResult : effectResults) {
                    List<? extends Action> actions = actionProxy.getOptionalAfterActions(playerId, actionResult);
                    List<Action> playableActions = getPlayableActions(playerId, actions);
                    result.addAll(playableActions);
                }
            }
        }

        return result;
    }

    private List<Action> getPlayableActions(String playerId, Iterable<? extends Action> actions) {
        List<Action> result = new LinkedList<>();
        if (actions != null) {
            for (Action action : actions) {
                if (_game.getModifiersQuerying().canPerformAction(playerId, action) && action.canBeInitiated(_game))
                    result.add(action);
            }
        }
        return result;
    }

    @Override
    public List<Action> getPhaseActions(String playerId) {
        List<Action> result = new LinkedList<>();

        for (ActionProxy actionProxy : _actionProxies) {
            List<? extends Action> actions = actionProxy.getPhaseActions(playerId);
            List<Action> playableActions = getPlayableActions(playerId, actions);
            result.addAll(playableActions);
        }

        return result;
    }

    @Override
    public void addActionToStack(Action action) {
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

}