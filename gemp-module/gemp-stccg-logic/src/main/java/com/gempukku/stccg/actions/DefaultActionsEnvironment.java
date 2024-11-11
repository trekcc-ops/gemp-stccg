package com.gempukku.stccg.actions;

import com.gempukku.stccg.game.DefaultGame;

import java.util.*;

public class DefaultActionsEnvironment implements ActionsEnvironment {
    private final DefaultGame _game;
    private final Stack<Action> _actionStack;
    private final List<ActionProxy> _actionProxies = new LinkedList<>();
    private final List<ActionProxy> _untilEndOfTurnActionProxies = new LinkedList<>();
    private final Map<Integer, Action> _performedActions = new HashMap<>();
    private Set<EffectResult> _effectResults = new HashSet<>();
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
            _performedActions.put(action.getActionId(), action);
        }
    }

    public DefaultGame getGame() { return _game; }

    @Override
    public void emitEffectResult(EffectResult effectResult) {
        _effectResults.add(effectResult);
    }

    public Set<EffectResult> consumeEffectResults() {
        Set<EffectResult> result = _effectResults;
        _effectResults = new HashSet<>();
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
    public List<Action> getRequiredBeforeTriggers(Effect effect) {
        List<Action> gatheredActions = new LinkedList<>();

        for (ActionProxy actionProxy : _actionProxies) {
            List<? extends Action> actions = actionProxy.getRequiredBeforeTriggers(effect);
            if (actions != null) {
                gatheredActions.addAll(actions);
            }
        }

        return gatheredActions;
    }

    @Override
    public List<Action> getOptionalBeforeTriggers(String playerId, Effect effect) {
        List<Action> result = new LinkedList<>();

        for (ActionProxy actionProxy : _actionProxies) {
            List<? extends Action> actions = actionProxy.getOptionalBeforeTriggerActions(playerId, effect);
            if (actions != null) result.addAll(actions);
        }

        return result;
    }

    @Override
    public List<Action> getOptionalBeforeActions(String playerId, Effect effect) {
        List<Action> result = new LinkedList<>();

        for (ActionProxy actionProxy : _actionProxies) {
            List<? extends Action> actions = actionProxy.getOptionalBeforeActions(playerId, effect);
            if (actions != null) {
                actions.stream().filter(action ->
                        _game.getModifiersQuerying().canPlayAction(playerId, action)).forEach(result::add);
            }
        }

        return result;
    }

    @Override
    public List<Action> getRequiredAfterTriggers(Collection<? extends EffectResult> effectResults) {
        List<Action> gatheredActions = new LinkedList<>();

        if (effectResults != null) {
            for (ActionProxy actionProxy : _actionProxies) {
                for (EffectResult effectResult : effectResults) {
                    List<? extends Action> actions = actionProxy.getRequiredAfterTriggers(effectResult);
                    if (actions != null)
                        gatheredActions.addAll(actions);
                }
            }
        }

        return gatheredActions;
    }

    @Override
    public Map<Action, EffectResult> getOptionalAfterTriggers(String playerId,
                                                              Collection<? extends EffectResult> effectResults) {
        final Map<Action, EffectResult> gatheredActions = new HashMap<>();

        if (effectResults != null) {
            for (EffectResult effectResult : effectResults) {
                List<? extends Action> actions = effectResult.getOptionalAfterTriggerActions(
                        _game.getGameState().getPlayer(playerId));
                if (actions != null) {
                    for (Action action : actions) {
                        if (!effectResult.wasOptionalTriggerUsed(action)) {
                            gatheredActions.put(action, effectResult);
                        }
                    }
                }
            }
        }

        return gatheredActions;
    }

    @Override
    public List<Action> getOptionalAfterActions(String playerId, Collection<? extends EffectResult> effectResults) {
        List<Action> result = new LinkedList<>();

        if (effectResults != null) {
            for (ActionProxy actionProxy : _actionProxies) {
                for (EffectResult effectResult : effectResults) {
                    List<? extends Action> actions = actionProxy.getOptionalAfterActions(playerId, effectResult);
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
                if (_game.getModifiersQuerying().canPlayAction(playerId, action))
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
        action.setId(_nextActionId);
        _nextActionId++;
    }

    public Stack<Action> getActionStack() { return _actionStack; }

    @Override
    public Map<Integer, Action> getPerformedActions() {
        return _performedActions;
    }

    @Override
    public boolean hasNoActionsInProgress() {
        return _actionStack.isEmpty();
    }

    @Override
    public void removeCompletedAction(Action action) {
        _actionStack.remove(action);
        _performedActions.put(action.getActionId(), action);
    }

    @Override
    public Action getCurrentAction() {
        return _actionStack.peek();
    }

}