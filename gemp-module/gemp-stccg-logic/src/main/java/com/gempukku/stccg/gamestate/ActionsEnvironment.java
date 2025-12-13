package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.game.ActionOrderOfOperationException;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.*;

public class ActionsEnvironment {
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

    public void addUntilEndOfTurnActionProxy(ActionProxy actionProxy) {
        _actionProxies.add(actionProxy);
        _untilEndOfTurnActionProxies.add(actionProxy);
    }


    public List<TopLevelSelectableAction> getPhaseActions(DefaultGame cardGame, Player player) {
        List<TopLevelSelectableAction> result = new LinkedList<>();

        for (ActionProxy actionProxy : _actionProxies) {
            for (TopLevelSelectableAction action : actionProxy.getPhaseActions(cardGame, player)) {
                if (action.canBeInitiated(cardGame)) {
                    result.add(action);
                }
            }
        }
        return result;
    }


    public void addActionToStack(Action action) {
        if (action != null) {
            action.startPerforming(); // Set action status
            _actionStack.add(action);
        }
    }

    public Stack<Action> getActionStack() { return _actionStack; }

    public List<Action> getPerformedActions() {
        return _performedActions;
    }

    public boolean hasNoActionsInProgress() {
        return _actionStack.isEmpty();
    }

    public void removeCompletedActionFromStack(Action action) throws ActionOrderOfOperationException {
        if (!action.isInProgress()) {
            _actionStack.remove(action);
            _performedActions.add(action);
        } else {
            throw new ActionOrderOfOperationException("Tried to remove incomplete action from stack of class " +
                    action.getClass().getSimpleName());
        }
    }

    public Action getCurrentAction() {
        if (_actionStack.isEmpty()) {
            return null;
        } else {
            return _actionStack.peek();
        }
    }

    public int getNextActionId() {
        return _nextActionId;
    }

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
            Action currentAction = getCurrentAction();
            currentAction.executeNextSubAction(this, cardGame);
        }
    }

    public Collection<ActionProxy> getAllActionProxies() {
        return _actionProxies;
    }

}