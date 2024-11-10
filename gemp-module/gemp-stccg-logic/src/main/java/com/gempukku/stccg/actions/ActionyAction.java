package com.gempukku.stccg.actions;

import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;

import java.util.Collections;
import java.util.LinkedList;

public abstract class ActionyAction implements Action {
    private String _cardActionPrefix;
    private final LinkedList<Action> _costs = new LinkedList<>();
    private final LinkedList<Action> _processedUsageCosts = new LinkedList<>();
    private final LinkedList<Action> _targeting = new LinkedList<>();
    private final LinkedList<Action> _processedCosts = new LinkedList<>();
    private final LinkedList<Action> _effects = new LinkedList<>();
    private final LinkedList<Action> _processedActions = new LinkedList<>();
    private final LinkedList<Action> _usageCosts = new LinkedList<>();
    protected String text;

    protected final String _performingPlayerId;
    private boolean _virtualCardAction;
    protected final ActionType _actionType;
    public ActionType getActionType() { return _actionType; }

    protected ActionyAction(String performingPlayerId, ActionType actionType) {
        _performingPlayerId = performingPlayerId;
        _actionType = actionType;
    }
    protected ActionyAction(Player player, ActionType actionType) {
        this(player.getPlayerId(), actionType);
    }

    protected ActionyAction(Player player, String text, ActionType actionType) {
        this(player.getPlayerId(), actionType);
        this.text = text;
    }

    protected ActionyAction(Action action) {
        this(action.getPerformingPlayerId(), action.getActionType());
    }
    protected ActionyAction() {
        _performingPlayerId = null;
        _actionType = ActionType.OTHER;
    }


    @Override
    public void setVirtualCardAction(boolean virtualCardAction) {
        _virtualCardAction = virtualCardAction;
    }

    @Override
    public boolean isVirtualCardAction() {
        return _virtualCardAction;
    }

    @Override
    public String getPerformingPlayerId() {
        return _performingPlayerId;
    }

    public final void appendCost(Action cost) {
        _costs.add(cost);
    }

    public final void appendTargeting(Action targeting) {
        _targeting.add(targeting);
    }

    public final void appendAction(Action action) {
        _effects.add(action);
    }

    public final void insertCost(Action cost) {
        _costs.addAll(0, Collections.singletonList(cost));
    }

    public final void insertAction(Action effect) {
        _effects.addAll(0, Collections.singletonList(effect));
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String getText(DefaultGame game) { return text; }

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
        final Action effect = _effects.poll();
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

    public SubAction createSubAction() {
        return new SubAction(this);
    }

    public abstract boolean canBeInitiated(DefaultGame cardGame);

/*    public boolean costsCanBePaid() {
                // TODO - This may have bugs if multiple costs exist that can be paid independently but not together
        for (Action effect : _costs)
            if (!effect.canBeInitiated()) {
                return false;
            }
        for (Action effect : _usageCosts)
            if (!effect.isPlayableInFull()) {
                return false;
            }
        return true;
    } */

    public void setCardActionPrefix(String prefix) {
        _cardActionPrefix = prefix;
    }

    public String getCardActionPrefix() { return _cardActionPrefix; }

    public final void appendUsage(Action cost) {
        if (!_costs.isEmpty() || !_processedCosts.isEmpty() || !_effects.isEmpty() || !_processedActions.isEmpty())
            throw new UnsupportedOperationException("Called appendUsage() in incorrect order");
        _usageCosts.add(cost);
    }

    public abstract Action nextAction(DefaultGame cardGame);
}