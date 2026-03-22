package com.gempukku.stccg.actions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.targetresolver.ActionTargetResolver;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.gamestate.ActionsEnvironment;
import com.gempukku.stccg.player.Player;

import java.util.LinkedList;
import java.util.List;

public abstract class ActionyAction implements Action {
    @JsonProperty("actionId")
    private int _actionId;
    protected final List<ActionTargetResolver> _cardTargets = new LinkedList<>();

    protected final String _performingPlayerId;
    protected final ActionType _actionType;
    private ActionResult _currentResult;
    protected final GameTextContext _actionContext;

    @JsonProperty("status")
    private ActionStatus _actionStatus;

    protected ActionyAction(int actionId, ActionType actionType, String performingPlayerId,
                          GameTextContext actionContext, ActionStatus status) {
        _actionContext = actionContext;
        _actionId = actionId;
        _actionType = actionType;
        _performingPlayerId = performingPlayerId;
        _actionStatus = status;
    }


    private ActionyAction(DefaultGame cardGame, ActionType actionType, String performingPlayerId,
                            GameTextContext actionContext) {
        this(cardGame.getActionsEnvironment().getNextActionId(), actionType, performingPlayerId, actionContext,
                ActionStatus.virtual);
        ActionsEnvironment environment = cardGame.getActionsEnvironment();
        environment.logAction(this);
        environment.incrementActionId();
    }


    protected ActionyAction(DefaultGame cardGame, String playerName, ActionType actionType, GameTextContext actionContext) {
        this(cardGame, actionType, playerName, actionContext);
    }

    protected ActionyAction(DefaultGame cardGame, String playerName, ActionType actionType) {
        this(cardGame, actionType, playerName, null);
    }

    protected ActionyAction(DefaultGame cardGame, Player player, ActionType actionType) {
        this(cardGame, actionType, player.getPlayerId(), null);
    }


    @Override
    public String getPerformingPlayerId() {
        return _performingPlayerId;
    }
    public ActionType getActionType() { return _actionType; }

    public final boolean wasInitiated() {
        return _actionStatus.wasInitiated();
    }

    public boolean canBeInitiated(DefaultGame cardGame) {
        return requirementsAreMet(cardGame) &&
                !cardGame.playerRestrictedFromPerformingActionDueToModifiers(_performingPlayerId, this) &&
                !wasInitiated();
    }

    public abstract boolean requirementsAreMet(DefaultGame cardGame);

    public int getActionId() { return _actionId; }


    public void startPerforming() {
        _actionStatus = ActionStatus.initiation_started;
    }

    public void setAsFailed() {
        if (_actionStatus == ActionStatus.initiation_started) {
            _actionStatus = ActionStatus.initiation_failed;
        } else {
            _actionStatus = ActionStatus.completed_failure;
        }
    }

    protected void setAsSuccessful() {
        _actionStatus = ActionStatus.completed_success;
    }

    public void cancel() { _actionStatus = ActionStatus.cancelled; }

    @JsonIgnore
    protected boolean isInProgress() {
        return _actionStatus.isInProgress();
    }

    public boolean wasCompleted() {
        return switch(_actionStatus) {
            case completed_success, completed_failure -> true;
            case virtual, initiation_failed, cancelled, initiation_started, initiation_complete -> false;
        };
    }

    public boolean wasFailed() {
        return _actionStatus.wasFailed();
    }

    public boolean wasSuccessful() {
        return _actionStatus == ActionStatus.completed_success;
    }


    protected void saveResult(ActionResult actionResult, DefaultGame cardGame) {
        _currentResult = actionResult;
        actionResult.initialize(cardGame);
    }

    public ActionResult getResult() { return _currentResult; }

    @JsonProperty("actionId")
    public void setActionId(int actionId) {
        _actionId = actionId;
    }

    public void executeNextSubAction(ActionsEnvironment actionsEnvironment, DefaultGame cardGame)
            throws InvalidGameOperationException {

        if (_currentResult != null) {
            if (_currentResult.canBeRespondedTo()) {
                _currentResult.addNextActionToStack(cardGame);
            } else {
                _currentResult = null;
            }
        } else if (!isInProgress()) {
            actionsEnvironment.removeActionFromStack(this);
        } else if (!wasInitiated()) {
            continueInitiation(cardGame);
        } else {
            processEffect(cardGame);
        }

        if (!isInProgress()) {
            actionsEnvironment.logCompletedAction(this);
        }
        cardGame.getGameState().checkVictoryConditions(cardGame);
        cardGame.sendActionResultToClient();
    }

    protected void continueInitiation(DefaultGame cardGame) throws InvalidGameLogicException {
        if (!_cardTargets.isEmpty()) {
            resolveNextTarget(cardGame);
        } else {
            _actionStatus = ActionStatus.initiation_complete;
            if (this instanceof ActionWithRespondableInitiation respondableAction) {
                respondableAction.saveInitiationResult(cardGame);
            }
        }
    }

    protected abstract void processEffect(DefaultGame cardGame);

    protected final void resolveNextTarget(DefaultGame cardGame) throws InvalidGameLogicException {
        ActionTargetResolver resolver = _cardTargets.getFirst();
        if (resolver.isResolved()) {
            _cardTargets.remove(resolver);
        } else if (resolver.cannotBeResolved(cardGame)) {
            this.setAsFailed();
        } else {
            resolver.resolve(cardGame);
        }
    }

    public void setAsInitiated() {
        _actionStatus = ActionStatus.initiation_complete;
    }

    public boolean hasOncePerGameLimit() {
        return false;
    }

}