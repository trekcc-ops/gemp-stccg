package com.gempukku.stccg.actions;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.NonEmptyListFilter;
import com.gempukku.stccg.game.ActionOrderOfOperationException;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.gamestate.ActionsEnvironment;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.*;

public abstract class ActionyAction implements Action {
    private String _cardActionPrefix;
    protected Map<String, Boolean> _progressIndicators = new HashMap<>();
    protected boolean _wasCarriedOut;
    private final int _actionId;
    private final LinkedList<Action> _costs = new LinkedList<>();
    private final LinkedList<Action> _processedUsageCosts = new LinkedList<>();
    private final LinkedList<Action> _targeting = new LinkedList<>();
    private final LinkedList<Action> _processedCosts = new LinkedList<>();
    private final LinkedList<Action> _actionEffects = new LinkedList<>();
    private final LinkedList<Action> _processedActions = new LinkedList<>();
    private final LinkedList<Action> _usageCosts = new LinkedList<>();

    protected final String _performingPlayerId;
    protected final ActionType _actionType;
    private ActionResult _currentResult;
    protected final ActionContext _actionContext;

    @JsonProperty("status")
    private ActionStatus _actionStatus;

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



    protected ActionyAction(DefaultGame cardGame, Player player, ActionType actionType, Enum<?>[] progressValues) {
        this(cardGame.getActionsEnvironment(), actionType, player.getPlayerId());
        for (Enum<?> progressType : progressValues) {
            _progressIndicators.put(progressType.name(), false);
        }
    }

    protected ActionyAction(DefaultGame cardGame, String performingPlayerName, ActionType actionType, Enum<?>[] progressValues) {
        this(cardGame.getActionsEnvironment(), actionType, performingPlayerName);
        for (Enum<?> progressType : progressValues) {
            _progressIndicators.put(progressType.name(), false);
        }
    }

    protected ActionyAction(DefaultGame cardGame, Player player, String text, ActionType actionType,
                            Enum<?>[] progressTypes) {
        this(cardGame.getActionsEnvironment(), actionType, player.getPlayerId());
        for (Enum<?> progressType : progressTypes) {
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


    protected ActionyAction(DefaultGame cardGame, String performingPlayerName, String text, ActionType actionType,
                            Enum<?>[] progressTypes) {
        this(cardGame.getActionsEnvironment(), actionType, performingPlayerName);
        for (Enum<?> progressType : progressTypes) {
            _progressIndicators.put(progressType.name(), false);
        }
    }


    // This constructor is only used for system queue actions
    protected ActionyAction(DefaultGame game, ActionType type) {
        this(game.getActionsEnvironment(), type, null);
    }

    // This constructor is only used for system queue actions
    protected ActionyAction(DefaultGame game, ActionType type, ActionContext actionContext) {
        this(game.getActionsEnvironment(), type, null, actionContext);
    }



    @Override
    public String getPerformingPlayerId() {
        return _performingPlayerId;
    }
    public ActionType getActionType() { return _actionType; }

    public final void appendCost(Action cost) {
        _costs.add(cost);
    }

    public final void appendTargeting(Action targeting) {
        _targeting.add(targeting);
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

    protected boolean isCostFailed() {
        for (Action processedCost : _processedCosts) {
            if (!processedCost.wasCarriedOut())
                return true;
        }
        for (Action usageCost : _processedUsageCosts) {
            if (!usageCost.wasCarriedOut())
                return true;
        }
        return false;
    }

    protected final Action getNextCost() {
        Action targetingCost = _targeting.poll();
        if (targetingCost != null) {
            _processedCosts.add(targetingCost);
            return targetingCost;
        }

        Action usageCost = _usageCosts.poll();
        if (usageCost != null) {
            _processedUsageCosts.add(usageCost);
            return usageCost;
        }

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

    public boolean wasCarriedOut() {
        if (isCostFailed())
            return false;

        for (Action processedAction : _processedActions) {
            if (!processedAction.wasCarriedOut())
                return false;
        }

        return true;
    }

    public boolean canBeInitiated(DefaultGame cardGame) {
        return requirementsAreMet(cardGame) && costsCanBePaid(cardGame);
    }

    public abstract boolean requirementsAreMet(DefaultGame cardGame);

    public boolean costsCanBePaid(DefaultGame game) {
        // TODO - This may not accurately show if not all costs can be paid
        // TODO - Not sure on the legality here. Is it legal to initiate an action if you can't fully pay the costs?
        for (Action cost : _costs)
            if (!cost.canBeInitiated(game)) {
                return false;
            }
        for (Action usageCost : _usageCosts)
            if (!usageCost.canBeInitiated(game)) {
                return false;
            }
        return true;
    }

    public void setCardActionPrefix(String prefix) {
        _cardActionPrefix = prefix;
    }

    public String getCardActionPrefix() { return _cardActionPrefix; }

    public final void appendUsage(Action cost) {
        if (!_costs.isEmpty() || !_processedCosts.isEmpty() || !_actionEffects.isEmpty() || !_processedActions.isEmpty())
            throw new UnsupportedOperationException("Called appendUsage() in incorrect order");
        _usageCosts.add(cost);
    }

    public abstract Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException, CardNotFoundException, PlayerNotFoundException, InvalidGameOperationException;

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

    @JsonIgnore
    public boolean isBeingInitiated() { return _actionStatus == ActionStatus.initiation_started; }

    public void startPerforming() throws ActionOrderOfOperationException {
        if (_actionStatus == ActionStatus.virtual) {
            _actionStatus = ActionStatus.initiation_started;
        } else {
            throw new ActionOrderOfOperationException("Tried to start performing an action already in progress");
        }
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

    protected void saveResult(ActionResult actionResult) {
        _currentResult = actionResult;
    }

    public void clearResult() {
        _currentResult = null;
    }

    public ActionResult getResult() { return _currentResult; }

    public ActionContext getContext() { return _actionContext; }

}