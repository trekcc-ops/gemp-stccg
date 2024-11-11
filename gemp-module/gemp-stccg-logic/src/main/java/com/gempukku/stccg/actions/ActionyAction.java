package com.gempukku.stccg.actions;

import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;

import java.util.Collections;
import java.util.LinkedList;

public abstract class ActionyAction implements Action {
    private String _cardActionPrefix;
    protected boolean _wasCarriedOut;
    private int _actionId;
    private final LinkedList<Action> _costs = new LinkedList<>();
    private final LinkedList<Action> _processedUsageCosts = new LinkedList<>();
    private final LinkedList<Action> _targeting = new LinkedList<>();
    private final LinkedList<Action> _processedCosts = new LinkedList<>();
    private final LinkedList<Action> _effects = new LinkedList<>();
    private final LinkedList<Action> _processedActions = new LinkedList<>();
    private final LinkedList<Action> _usageCosts = new LinkedList<>();
    protected String _text;

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
        this._text = text;
    }

    protected ActionyAction(Action action) {
        this(action.getPerformingPlayerId(), action.getActionType());
    }
    protected ActionyAction() {
        _performingPlayerId = null;
        _actionType = ActionType.OTHER;
    }

    public Effect nextEffect(DefaultGame game) { return null; }


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

    public final void insertAction(Action action) {
        _effects.addAll(0, Collections.singletonList(action));
    }

    /**
     * Sets the text shown for the action selection on the User Interface.
     * @param text the text to show for the action selection
     */
    public void setText(String text) {
        _text = text;
    }

    @Override
    public String getText(DefaultGame game) { return _text; }

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
        if (!_costs.isEmpty() || !_processedCosts.isEmpty() || !_effects.isEmpty() || !_processedActions.isEmpty())
            throw new UnsupportedOperationException("Called appendUsage() in incorrect order");
        _usageCosts.add(cost);
    }

    public final void appendUsage(Effect effect) {
        appendUsage(new SubAction(this, effect));
    }
    public final void insertEffect(Effect effect) { insertAction(new SubAction(this, effect)); }

    public final void appendEffect(Effect effect) { appendAction(new SubAction(this, effect)); }

    public abstract Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException;

    public void insertCost(Effect effect) { insertCost(new SubAction(this, effect)); }

    public final void appendCost(Effect effect) { appendCost(new SubAction(this, effect)); }
    public void appendTargeting(Effect effect) { appendCost(new SubAction(this, effect)); }
    public void setId(int id) { _actionId = id; }
    public int getActionId() { return _actionId; }

}