package com.gempukku.stccg.actions;

import com.gempukku.stccg.game.DefaultGame;

import java.util.LinkedList;

public abstract class AbstractCostToEffectAction implements CostToEffectAction {
    private String _cardActionPrefix;
    int _actionId;
    protected final LinkedList<Effect> _costs = new LinkedList<>();
    private final LinkedList<Effect> _processedUsageCosts = new LinkedList<>();
    private final LinkedList<Effect> _targeting = new LinkedList<>();
    private final LinkedList<Effect> _processedCosts = new LinkedList<>();
    protected final LinkedList<Effect> _effects = new LinkedList<>();
    private final LinkedList<Effect> _processedEffects = new LinkedList<>();
    private final LinkedList<Effect> _usageCosts = new LinkedList<>();
    protected String text;

    protected final String _performingPlayerId;
    private boolean _virtualCardAction;
    protected final ActionType _actionType;
    public ActionType getActionType() { return _actionType; }

    protected AbstractCostToEffectAction(DefaultGame game, Action action) {
        _performingPlayerId = action.getPerformingPlayerId();
        _actionType = action.getActionType();
        _actionId = game.getActionsEnvironment().getNextActionId();
        game.getActionsEnvironment().incrementActionId();
    }


    protected AbstractCostToEffectAction(Action action) {
        _performingPlayerId = action.getPerformingPlayerId();
        _actionType = action.getActionType();
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

    @Override
    public final void appendCost(Effect cost) {
        _costs.add(cost);
    }

    public final void appendTargeting(Effect targeting) {
        _targeting.add(targeting);
    }

    @Override
    public final void appendEffect(Effect effect) {
        _effects.add(effect);
    }

    @Override
    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String getActionSelectionText(DefaultGame game) { return text; }

    protected boolean isCostFailed() {
        for (Effect processedCost : _processedCosts) {
            if (!processedCost.wasCarriedOut())
                return true;
        }
        for (Effect usageCost : _processedUsageCosts) {
            if (!usageCost.wasCarriedOut())
                return true;
        }
        return false;
    }

    protected final Effect getNextCost() {
        Effect targetingCost = _targeting.poll();
        if (targetingCost != null) {
            _processedCosts.add(targetingCost);
            return targetingCost;
        }

        Effect usageCost = _usageCosts.poll();
        if (usageCost != null) {
            _processedUsageCosts.add(usageCost);
            return usageCost;
        }

        Effect cost = _costs.poll();
        if (cost != null)
            _processedCosts.add(cost);
        return cost;
    }

    protected final Effect getNextEffect() {
        final Effect effect = _effects.poll();
        if (effect != null)
            _processedEffects.add(effect);
        return effect;
    }

    public boolean wasCarriedOut() {
        if (isCostFailed())
            return false;

        for (Effect processedEffect : _processedEffects) {
            if (!processedEffect.wasCarriedOut())
                return false;
        }

        return true;
    }

    public boolean canBeInitiated(DefaultGame cardGame) {
        return costsCanBePaid();
    }

    public boolean costsCanBePaid() {
                // TODO - This may have bugs if multiple costs exist that can be paid independently but not together
        for (Effect effect : _costs)
            if (!effect.isPlayableInFull()) {
                return false;
            }
        for (Effect effect : _usageCosts)
            if (!effect.isPlayableInFull()) {
                return false;
            }
        return true;
    }

    public void setCardActionPrefix(String prefix) {
        _cardActionPrefix = prefix;
    }

    public String getCardActionPrefix() { return _cardActionPrefix; }

}