package com.gempukku.stccg.actions;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.gempukku.stccg.actions.blueprints.SubActionBlueprint;
import com.gempukku.stccg.actions.blueprints.UsePerGameLimitActionBlueprint;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.NonEmptyListFilter;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public abstract class ActionWithSubActions extends ActionyAction {

    protected final LinkedList<SubActionBlueprint> _queuedCosts = new LinkedList<>();
    protected final LinkedList<SubActionBlueprint> _queuedSubActions = new LinkedList<>();
    protected Action _currentSubAction;
    protected final LinkedList<Action> _processedSubActions = new LinkedList<>();
    protected Action _currentCostBeingPaid;
    protected final LinkedList<Action> _processedCosts = new LinkedList<>();


    public ActionWithSubActions(int actionId, ActionType actionType, String performingPlayerName,
                                ActionStatus status, GameTextContext context) {
        super(actionId, actionType, performingPlayerName, context, status);
    }

    public ActionWithSubActions(DefaultGame cardGame, String performingPlayerId, ActionType actionType,
                                GameTextContext context) {
        super(cardGame, performingPlayerId, actionType, context);
    }

    @Override
    protected void processEffect(DefaultGame cardGame) {
        if (_currentSubAction == null && !_queuedSubActions.isEmpty()) {
            try {
                SubActionBlueprint nextSubAction = _queuedSubActions.getFirst();
                _queuedSubActions.removeFirst();
                if (nextSubAction.isPlayableInFull(cardGame, _actionContext)) {
                    _currentSubAction = nextSubAction.createAction(cardGame, _actionContext);
                    cardGame.getActionsEnvironment().addActionToStack(_currentSubAction);
                } else {
                    setAsFailed();
                }
            } catch(Exception exp) {
                cardGame.sendErrorMessage(exp);
                setAsFailed();
            }
        } else if (_currentSubAction != null && _currentSubAction.wasSuccessful()) {
            _processedSubActions.add(_currentSubAction);
            _currentSubAction = null;
        } else if (_currentSubAction != null && _currentSubAction.wasFailed()) {
            _processedSubActions.add(_currentSubAction);
            _currentSubAction = null;
            setAsFailed();
        } else {
            setAsSuccessful();
        }
    }

    @Override
    public boolean hasOncePerGameLimit() {
        for (SubActionBlueprint costAction : _queuedCosts) {
            if (costAction instanceof UsePerGameLimitActionBlueprint) {
                return true;
            }
        }
        return false;
    }

    public final void appendSubAction(SubActionBlueprint subAction) {
        _queuedSubActions.add(subAction);
    }


    public abstract PhysicalCard getPerformingCard();

    public void insertSubActions(Collection<SubActionBlueprint> subActions) {
        _queuedSubActions.addAll(0, subActions);
    }

    public void removeRemainingSubActions() {
        _queuedSubActions.clear();
    }

    public final void appendCost(SubActionBlueprint cost) { _queuedCosts.add(cost); }

    protected final void payNextCost(DefaultGame cardGame) {
        if (_currentCostBeingPaid != null) {
            if (_currentCostBeingPaid.wasFailed()) {
                setAsFailed();
            }
            _processedCosts.add(_currentCostBeingPaid);
            _currentCostBeingPaid = null;
        } else if (!_queuedCosts.isEmpty()) {
            Action cost = _queuedCosts.getFirst().createAction(cardGame, _actionContext);
            if (cost == null) {
                setAsFailed();
            } else {
                _queuedCosts.removeFirst();
                _currentCostBeingPaid = cost;
                cardGame.addActionToStack(_currentCostBeingPaid);
            }
        }
    }

    public boolean costsCanBePaid(DefaultGame game) {
        // TODO - This may not accurately show if not all costs can be paid
        // TODO - Not sure on the legality here. Is it legal to initiate an action if you can't fully pay the costs?
        for (SubActionBlueprint cost : _queuedCosts) {
            if (!cost.isPlayableInFull(game, _actionContext)) {
                return false;
            }
        }
        return true;
    }

    public final boolean canBeInitiated(DefaultGame cardGame) {
        return requirementsAreMet(cardGame) && costsCanBePaid(cardGame) &&
                !cardGame.playerRestrictedFromPerformingActionDueToModifiers(_performingPlayerId, this) &&
                !wasInitiated();
    }

    protected void continueInitiation(DefaultGame cardGame) throws InvalidGameLogicException {
        if (!_cardTargets.isEmpty()) {
            resolveNextTarget(cardGame);
        } else if (!_queuedCosts.isEmpty() || _currentCostBeingPaid != null) {
            payNextCost(cardGame);
        } else {
            setAsInitiated();
            if (this instanceof ActionWithRespondableInitiation respondableAction) {
                respondableAction.saveInitiationResult(cardGame);
            }
        }
    }

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NonEmptyListFilter.class)
    @JsonIdentityReference(alwaysAsId=true)
    public List<SubActionBlueprint> getCosts() { return _queuedCosts; }

}