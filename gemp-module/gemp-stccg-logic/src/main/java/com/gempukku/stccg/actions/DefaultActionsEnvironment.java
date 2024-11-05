package com.gempukku.stccg.actions;

import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.SnapshotData;
import com.gempukku.stccg.game.DefaultGame;

import java.util.*;

public class DefaultActionsEnvironment implements ActionsEnvironment {
    private final DefaultGame _game;
    private final Stack<Action> _actionStack;
    private final List<ActionProxy> _actionProxies = new LinkedList<>();
    private final Map<Phase, List<ActionProxy>> _untilStartOfPhaseActionProxies = new HashMap<>();
    private final Map<Phase, List<ActionProxy>> _untilEndOfPhaseActionProxies = new HashMap<>();
    private final List<ActionProxy> _untilEndOfTurnActionProxies = new LinkedList<>();
    private final List<Action> _performedActions = new LinkedList<>();

    private Set<EffectResult> _effectResults = new HashSet<>();

    private final List<EffectResult> turnEffectResults = new LinkedList<>();
    private final List<EffectResult> phaseEffectResults = new LinkedList<>();

    public DefaultActionsEnvironment(DefaultGame game, Stack<Action> actionStack) {
        _game = game;
        _actionStack = actionStack;
    }

    private DefaultActionsEnvironment(DefaultGame game, Stack<Action> actionStack,
                                      Collection<? extends ActionProxy> actionProxies,
                                      Collection<? extends ActionProxy> untilEndOfTurnActionProxies) {
        _game = game;
        _actionStack = actionStack;
        _actionProxies.addAll(actionProxies);
        _untilEndOfTurnActionProxies.addAll(untilEndOfTurnActionProxies);
    }

    public DefaultActionsEnvironment generateSnapshot(SnapshotData snapshotData) {
        // Snapshot should not be created if effect results lists have members
        if (!_effectResults.isEmpty() || !turnEffectResults.isEmpty() || !phaseEffectResults.isEmpty()) {
            throw new UnsupportedOperationException(
                    "Cannot generate snapshot of DefaultActionsEnvironment with EffectResults"
            );
        }

        Stack<Action> newActionStack = new Stack<>();
        for (Action action : _actionStack)
            newActionStack.add(snapshotData.getDataForSnapshot(action));

        return new DefaultActionsEnvironment(_game, newActionStack, _actionProxies, _untilEndOfTurnActionProxies);
    }

    public DefaultGame getGame() { return _game; }

    public List<ActionProxy> getUntilStartOfPhaseActionProxies(Phase phase) {
        return _untilStartOfPhaseActionProxies.get(phase);
    }

    @Override
    public void emitEffectResult(EffectResult effectResult) {
        _effectResults.add(effectResult);
        if (Objects.equals(_game.getStatus(), "Playing")) turnEffectResults.add(effectResult);
        phaseEffectResults.add(effectResult);
    }

    public Set<EffectResult> consumeEffectResults() {
        Set<EffectResult> result = _effectResults;
        _effectResults = new HashSet<>();
        return result;
    }

    public void addAlwaysOnActionProxy(ActionProxy actionProxy) {
        _actionProxies.add(actionProxy);
    }

    public void signalStartOfPhase(Phase phase) {
        List<ActionProxy> list = _untilStartOfPhaseActionProxies.get(phase);
        if (list != null) {
            _actionProxies.removeAll(list);
            list.clear();
        }
    }

    public void signalEndOfPhase() {
        List<ActionProxy> list = _untilEndOfPhaseActionProxies.get(_game.getGameState().getCurrentPhase());
        if (list != null) {
            _actionProxies.removeAll(list);
            list.clear();
        }
        phaseEffectResults.clear();
    }

    public void signalEndOfTurn() {
        _actionProxies.removeAll(_untilEndOfTurnActionProxies);
        _untilEndOfTurnActionProxies.clear();
        turnEffectResults.clear();
    }

    @Override
    public List<EffectResult> getTurnEffectResults() {
        return Collections.unmodifiableList(turnEffectResults);
    }

    @Override
    public List<EffectResult> getPhaseEffectResults() {
        return Collections.unmodifiableList(phaseEffectResults);
    }

    @Override
    public void addUntilStartOfPhaseActionProxy(ActionProxy actionProxy, Phase phase) {
        _actionProxies.add(actionProxy);
        List<ActionProxy> list = _untilStartOfPhaseActionProxies.computeIfAbsent(phase, k -> new LinkedList<>());
        list.add(actionProxy);
    }

    @Override
    public void addUntilEndOfPhaseActionProxy(ActionProxy actionProxy, Phase phase) {
        _actionProxies.add(actionProxy);
        List<ActionProxy> list = _untilEndOfPhaseActionProxies.computeIfAbsent(phase, k -> new LinkedList<>());
        list.add(actionProxy);
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
    public Map<Action, EffectResult> getOptionalAfterTriggers(String playerId, Collection<? extends EffectResult> effectResults) {
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
                    if (actions != null) {
                        for (Action action : actions) {
                            if (_game.getModifiersQuerying().canPlayAction(playerId, action))
                                result.add(action);
                        }
                    }
                }
            }
        }

        return result;
    }

    @Override
    public List<Action> getPhaseActions(String playerId) {
        List<Action> result = new LinkedList<>();

        for (ActionProxy actionProxy : _actionProxies) {
            List<? extends Action> actions = actionProxy.getPhaseActions(playerId);
            if (actions != null) {
                for (Action action : actions) {
                    if (_game.getModifiersQuerying().canPlayAction(playerId, action))
                        result.add(action);
                }
            }
        }

        return result;
    }

    @Override
    public void addActionToStack(Action action) {
        _actionStack.add(action);
    }

    public Stack<Action> getActionStack() { return _actionStack; }

    public void addPerformedAction(Action action) { _performedActions.add(action); }

    @Override
    public List<Action> getPerformedActions() {
        return _performedActions;
    }

}