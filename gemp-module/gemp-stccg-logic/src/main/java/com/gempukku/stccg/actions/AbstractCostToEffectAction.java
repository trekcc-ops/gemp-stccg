package com.gempukku.stccg.actions;

import com.gempukku.stccg.actions.turn.UsageEffect;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;

import java.util.Collections;
import java.util.LinkedList;

public abstract class AbstractCostToEffectAction implements CostToEffectAction {
    private String _cardActionPrefix;
    private final LinkedList<Effect> _costs = new LinkedList<>();
    private final LinkedList<Effect> _processedUsageCosts = new LinkedList<>();
    private final LinkedList<Effect> _targeting = new LinkedList<>();
    private final LinkedList<Effect> _processedCosts = new LinkedList<>();
    private final LinkedList<Effect> _effects = new LinkedList<>();
    private final LinkedList<Effect> _processedEffects = new LinkedList<>();
    private final LinkedList<Effect> _usageCosts = new LinkedList<>();
    protected String text;

    protected final String _performingPlayerId;
    protected final Action _thisAction = this;
    private boolean _virtualCardAction = false;
    protected final ActionType _actionType;
    public ActionType getActionType() { return _actionType; }

    protected AbstractCostToEffectAction(String performingPlayerId, ActionType actionType) {
        _performingPlayerId = performingPlayerId;
        _actionType = actionType;
    }
    protected AbstractCostToEffectAction(Player player, ActionType actionType) {
        this(player.getPlayerId(), actionType);
    }
    protected AbstractCostToEffectAction(Action action) {
        this(action.getPerformingPlayerId(), action.getActionType());
    }
    protected AbstractCostToEffectAction() {
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
    public final void insertCost(Effect cost) {
        _costs.addAll(0, Collections.singletonList(cost));
    }

    @Override
    public final void insertEffect(Effect effect) {
        _effects.addAll(0, Collections.singletonList(effect));
    }

    @Override
    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String getText() { return text; }

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

    public abstract DefaultGame getGame();

    public SubAction createSubAction() {
        return new SubAction(_thisAction);
    }

    public boolean canBeInitiated() {
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

    public final void appendUsage(UsageEffect cost) {
        if (!_costs.isEmpty() || !_processedCosts.isEmpty() || !_effects.isEmpty() || !_processedEffects.isEmpty())
            throw new UnsupportedOperationException("Called appendUsage() in incorrect order");
        _usageCosts.add(cost);
    }

}
