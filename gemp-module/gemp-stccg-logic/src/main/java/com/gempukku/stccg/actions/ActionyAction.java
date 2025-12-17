package com.gempukku.stccg.actions;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.targetresolver.ActionTargetResolver;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.NonEmptyListFilter;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.gamestate.ActionsEnvironment;
import com.gempukku.stccg.player.Player;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public abstract class ActionyAction implements Action {
    private String _cardActionPrefix;
    @JsonProperty("actionId")
    private int _actionId;
    protected final List<ActionTargetResolver> _cardTargets = new LinkedList<>();
    protected final LinkedList<Action> _costs = new LinkedList<>();
    protected final LinkedList<Action> _processedCosts = new LinkedList<>();

    protected final String _performingPlayerId;
    protected final ActionType _actionType;
    private ActionResult _currentResult;
    protected final ActionContext _actionContext;

    @JsonProperty("status")
    private ActionStatus _actionStatus;

    private ActionyAction(int actionId, ActionType actionType, String performingPlayerId,
                          ActionContext actionContext, ActionStatus status) {
        _actionContext = actionContext;
        _actionId = actionId;
        _actionType = actionType;
        _performingPlayerId = performingPlayerId;
        _actionStatus = status;
    }


    protected ActionyAction(DefaultGame cardGame, ActionType actionType, String performingPlayerId,
                            ActionContext actionContext) {
        this(cardGame.getActionsEnvironment().getNextActionId(), actionType, performingPlayerId, actionContext,
                ActionStatus.virtual);
        ActionsEnvironment environment = cardGame.getActionsEnvironment();
        environment.logAction(this);
        environment.incrementActionId();
    }

    protected ActionyAction(int actionId, ActionType actionType, String performingPlayerId, ActionStatus status) {
        this(actionId, actionType, performingPlayerId, null, status);
    }


    protected ActionyAction(DefaultGame cardGame, String playerName, ActionType actionType, ActionContext actionContext) {
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
    public final void appendCost(Action cost) {
        _costs.add(cost);
    }
    public final void insertCosts(Collection<Action> costs) {
        _costs.addAll(0, costs);
    }
    public final boolean wasInitiated() {
        return _actionStatus.wasInitiated();
    }

    public final boolean canBeInitiated(DefaultGame cardGame) {
        return requirementsAreMet(cardGame) && costsCanBePaid(cardGame) &&
                !cardGame.playerRestrictedFromPerformingActionDueToModifiers(_performingPlayerId, this) &&
                !wasInitiated();
    }

    public abstract boolean requirementsAreMet(DefaultGame cardGame);

    public boolean costsCanBePaid(DefaultGame game) {
        // TODO - This may not accurately show if not all costs can be paid
        // TODO - Not sure on the legality here. Is it legal to initiate an action if you can't fully pay the costs?
        for (Action cost : _costs)
            if (!cost.canBeInitiated(game)) {
                return false;
            }
        return true;
    }

    public void setCardActionPrefix(String prefix) {
        _cardActionPrefix = prefix;
    }

    public String getCardActionPrefix() { return _cardActionPrefix; }

    public int getActionId() { return _actionId; }

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NonEmptyListFilter.class)
    @JsonIdentityReference(alwaysAsId=true)
    public List<Action> getCosts() { return _costs; }

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
        cardGame.sendActionResultToClient();
    }

    protected void continueInitiation(DefaultGame cardGame) throws InvalidGameLogicException {
        if (!_cardTargets.isEmpty()) {
            resolveNextTarget(cardGame);
        } else if (!_costs.isEmpty()) {
            payNextCost(cardGame);
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

    protected final void payNextCost(DefaultGame cardGame) {
        Action costAction = _costs.getFirst();
        if (costAction.wasSuccessful()) {
            _costs.remove(costAction);
            _processedCosts.add(costAction);
        } else if (costAction.wasFailed()) {
            this.setAsFailed();
        } else {
            cardGame.addActionToStack(costAction);
        }
    }

}