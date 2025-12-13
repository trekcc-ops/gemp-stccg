package com.gempukku.stccg.actions;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.NonEmptyListFilter;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.gamestate.ActionsEnvironment;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.*;

public abstract class ActionyAction implements Action {
    private String _cardActionPrefix;
    protected Map<String, Boolean> _progressIndicators = new HashMap<>();
    @JsonProperty("actionId")
    private int _actionId;
    private final LinkedList<Action> _costs = new LinkedList<>();

    protected final List<ActionCardResolver> _cardTargets = new LinkedList<>();
    private final List<ActionCardResolver> _resolvedTargets = new LinkedList<>();
    private final LinkedList<Action> _processedCosts = new LinkedList<>();
    private final LinkedList<Action> _actionEffects = new LinkedList<>();
    private final LinkedList<Action> _processedActions = new LinkedList<>();

    protected final String _performingPlayerId;
    protected final ActionType _actionType;
    private ActionResult _currentResult;
    protected final ActionContext _actionContext;

    @JsonProperty("status")
    private ActionStatus _actionStatus;

    protected ActionyAction(int actionId, ActionType actionType, String performingPlayerId) {
        _actionId = actionId;
        _actionType = actionType;
        _performingPlayerId = performingPlayerId;
        _actionStatus = ActionStatus.virtual;
        _actionContext = null;
    }


    protected ActionyAction(ActionsEnvironment environment, ActionType actionType, String performingPlayerId) {
        _actionId = environment.getNextActionId();
        environment.logAction(this);
        environment.incrementActionId();
        _actionType = actionType;
        _performingPlayerId = performingPlayerId;
        _actionStatus = ActionStatus.virtual;
        _actionContext = null;
    }

    protected ActionyAction(ActionsEnvironment environment, ActionType actionType, String performingPlayerId,
                            ActionContext actionContext) {
        _actionContext = actionContext;
        _actionId = environment.getNextActionId();
        environment.logAction(this);
        environment.incrementActionId();
        _actionType = actionType;
        _performingPlayerId = performingPlayerId;
        _actionStatus = ActionStatus.virtual;
    }

    protected ActionyAction(DefaultGame cardGame, String playerName, ActionType actionType, ActionContext actionContext) {
        this(cardGame.getActionsEnvironment(), actionType, playerName, actionContext);
    }


    protected ActionyAction(DefaultGame cardGame, String playerName, ActionType actionType) {
        this(cardGame.getActionsEnvironment(), actionType, playerName);
    }

    protected ActionyAction(DefaultGame cardGame, Player player, ActionType actionType) {
        this(cardGame.getActionsEnvironment(), actionType, player.getPlayerId());
    }


    protected ActionyAction(DefaultGame cardGame, Player player, String text, ActionType actionType) {
        this(cardGame.getActionsEnvironment(), actionType, player.getPlayerId());
    }

    protected ActionyAction(DefaultGame cardGame, String performingPlayerName, String text, ActionType actionType) {
        this(cardGame.getActionsEnvironment(), actionType, performingPlayerName);
    }


    protected ActionyAction(DefaultGame cardGame, String performingPlayerName, ActionType actionType,
                            Enum<?>[] progressValues) {
        this(cardGame.getActionsEnvironment(), actionType, performingPlayerName);
        for (Enum<?> progressType : progressValues) {
            _progressIndicators.put(progressType.name(), false);
        }
    }

    protected ActionyAction(DefaultGame cardGame, String performingPlayerName, ActionType actionType,
                            Enum<?>[] progressTypes, ActionContext actionContext) {
        this(cardGame.getActionsEnvironment(), actionType, performingPlayerName, actionContext);
        for (Enum<?> progressType : progressTypes) {
            _progressIndicators.put(progressType.name(), false);
        }
    }


    @Override
    public String getPerformingPlayerId() {
        return _performingPlayerId;
    }
    public ActionType getActionType() { return _actionType; }

    public final void appendCost(Action cost) {
        _costs.add(cost);
    }

    public final void appendEffect(Action action) {
        _actionEffects.add(action);
    }

    public final void insertCost(Action cost) {
        _costs.addAll(0, Collections.singletonList(cost));
    }

    public final void insertCosts(Collection<Action> costs) {
        _costs.addAll(0, costs);
    }

    public final void insertAction(Action action) {
        _actionEffects.addAll(0, Collections.singletonList(action));
    }
    public final void insertActions(Collection<Action> actions) {
        _actionEffects.addAll(0, actions);
    }

    protected final Action getNextCost() {
        Action cost = _costs.poll();
        if (cost != null)
            _processedCosts.add(cost);
        return cost;
    }

    protected final Action getNextAction() {
        final Action effect = _actionEffects.poll();
        if (effect != null)
            _processedActions.add(effect);
        return effect;
    }

    public final boolean wasCarriedOut() {
        return wasSuccessful();
    }
    public final boolean wasInitiated() {
        return _actionStatus.wasInitiated();
    }

    public final boolean canBeInitiated(DefaultGame cardGame) {
        return requirementsAreMet(cardGame) && costsCanBePaid(cardGame) &&
                cardGame.playerRestrictedFromPerformingActionDueToModifiers(_performingPlayerId, this) &&
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
    protected void setProgress(Enum<?> progressType) {
        _progressIndicators.put(progressType.name(), true);
    }

    protected List<Action> getActions() { return _actionEffects; }

    protected boolean getProgress(Enum<?> progressType) {
        return _progressIndicators.get(progressType.name());
    }

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NonEmptyListFilter.class)
    @JsonIdentityReference(alwaysAsId=true)
    public List<Action> getCosts() { return _costs; }

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NonEmptyListFilter.class)
    public Map<String, Boolean> getProgressIndicators() {
        return _progressIndicators;
    }

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

    protected void setAsInitiated() {
        _actionStatus = ActionStatus.initiation_complete;
    }

    @JsonIgnore
    public boolean isInProgress() {
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

    public void clearResult() {
        _currentResult = null;
    }

    public ActionResult getResult() { return _currentResult; }

    @JsonProperty("actionId")
    public void setActionId(int actionId) {
        _actionId = actionId;
    }

    public void executeNextSubAction(ActionsEnvironment actionsEnvironment, DefaultGame cardGame)
            throws PlayerNotFoundException, InvalidGameLogicException {

        ActionResult actionResult = getResult();
        if (actionResult != null) {
            if (actionResult.canBeRespondedTo()) {
                actionResult.addNextActionToStack(cardGame);
            } else {
                clearResult();
            }
        } else if (!isInProgress() && getResult() == null) {
            actionsEnvironment.removeCompletedActionFromStack(this);
            cardGame.sendActionResultToClient();
        } else {
            if (isInProgress()) {
                if (!wasInitiated()) {
                    continueInitiation(cardGame);
                } else {
                    continueEffects(cardGame);
                }
            } else if (cardGame.isCarryingOutEffects() && getResult() == null) {
                throw new InvalidGameLogicException("Unable to process action");
            }
        }
    }

    protected void continueInitiation(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException {
        resolveTargets(cardGame);

        if (_cardTargets.isEmpty() && thisActionShouldBeContinued(cardGame)) {
            int loopNumber = 1;

            while (thisActionShouldBeContinued(cardGame) && !_costs.isEmpty()) {
                Action targetingAction = _costs.getFirst();
                if (targetingAction.wasSuccessful()) {
                    _costs.remove(targetingAction);
                    _processedCosts.add(targetingAction);
                } else if (targetingAction.wasFailed()) {
                    this.setAsFailed();
                } else {
                    cardGame.getActionsEnvironment().addActionToStack(targetingAction);
                }
                loopNumber++;
                if (loopNumber > 500) {
                    throw new InvalidGameLogicException("Looped more than 500 times through Action.continueInitiation " +
                            "method. This is likely due to a circular logic error.");
                }
            }
        }

        if (_cardTargets.isEmpty() && _costs.isEmpty() && thisActionShouldBeContinued(cardGame)) {
            _actionStatus = ActionStatus.initiation_complete;
        }
    }

    protected final void continueEffects(DefaultGame cardGame) throws InvalidGameLogicException {
        int loopNumber = 1;

        while (thisActionShouldBeContinued(cardGame) && !_actionEffects.isEmpty()) {
            Action subAction = _actionEffects.getFirst();
            if (subAction.wasSuccessful()) {
                _actionEffects.remove(subAction);
                _processedActions.add(subAction);
            } else if (subAction.wasFailed()) {
                this.setAsFailed();
            } else {
                cardGame.getActionsEnvironment().addActionToStack(subAction);
            }
            loopNumber++;
            if (loopNumber > 500) {
                throw new InvalidGameLogicException("Looped more than 500 times through Action.continueInitiation " +
                        "method. This is likely due to a circular logic error.");
            }
        }

        if (_actionEffects.isEmpty() && thisActionShouldBeContinued(cardGame)) {
            processEffect(cardGame);
        }
    }


    private boolean thisActionShouldBeContinued(DefaultGame cardGame) {
        return cardGame.getCurrentAction() == this && cardGame.getGameState().hasNoPendingDecisions() &&
                _actionStatus.isInProgress();
    }

    protected void processEffect(DefaultGame cardGame) {
        setAsSuccessful();
    }

    protected final void resolveTargets(DefaultGame cardGame) throws InvalidGameLogicException {
        int loopNumber = 1;

        while (thisActionShouldBeContinued(cardGame) && !_cardTargets.isEmpty()) {
            ActionCardResolver resolver = _cardTargets.getFirst();
            if (resolver.isResolved()) {
                _cardTargets.remove(resolver);
                _resolvedTargets.add(resolver);
            } else if (resolver.cannotBeResolved(cardGame)) {
                this.setAsFailed();
            } else {
                resolver.resolve(cardGame);
            }
            loopNumber++;
            if (loopNumber > 500) {
                throw new InvalidGameLogicException("Looped more than 500 times while resolving targets. " +
                        "This is likely due to a circular logic error.");
            }
        }
    }

}