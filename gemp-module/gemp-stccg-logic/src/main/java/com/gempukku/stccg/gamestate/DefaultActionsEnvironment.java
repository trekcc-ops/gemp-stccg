package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.PlayerNotFoundException;

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
    public List<TopLevelSelectableAction> getPhaseActions(Player player) {
        List<TopLevelSelectableAction> result = new LinkedList<>();

        for (ActionProxy actionProxy : _actionProxies) {
            List<TopLevelSelectableAction> actions = actionProxy.getPhaseActions(player);
            List<TopLevelSelectableAction> playableActions = getPlayableActions(player.getPlayerId(), actions);
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