package com.gempukku.stccg.actions;

import com.gempukku.stccg.actions.discard.DiscountEffect;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;

import java.util.Collections;
import java.util.LinkedList;

public abstract class AbstractCostToEffectAction implements CostToEffectAction {
    private final LinkedList<DiscountEffect> _potentialDiscounts = new LinkedList<>();
    private final LinkedList<DiscountEffect> _processedDiscounts = new LinkedList<>();
    private final LinkedList<Effect> _costs = new LinkedList<>();
    private final LinkedList<Effect> _targeting = new LinkedList<>();
    private final LinkedList<Effect> _processedCosts = new LinkedList<>();
    private final LinkedList<Effect> _effects = new LinkedList<>();
    private final LinkedList<Effect> _processedEffects = new LinkedList<>();
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
    public final void appendPotentialDiscount(DiscountEffect discount) {
        _potentialDiscounts.add(discount);
    }

    @Override
    public final void appendCost(Effect cost) {
        _costs.add(cost);
    }

    @Override
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
        return false;
    }

    protected int getProcessedDiscount() {
        int discount = 0;
        for (DiscountEffect processedDiscount : _processedDiscounts) {
            discount += processedDiscount.getDiscountPaidFor();
        }
        return discount;
    }

    protected int getPotentialDiscount() {
        int sum = 0;
        for (DiscountEffect potentialDiscount : _potentialDiscounts) {
            sum += potentialDiscount.getMaximumPossibleDiscount();
        }
        return sum;
    }

    protected final Effect getNextCost() {
        Effect targetingCost = _targeting.poll();
        if (targetingCost != null) {
            _processedCosts.add(targetingCost);
            return targetingCost;
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

    protected final DiscountEffect getNextPotentialDiscount() {
        DiscountEffect discount = _potentialDiscounts.poll();
        if (discount != null)
            _processedDiscounts.add(discount);
        return discount;
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

    public boolean canBeInitiated() { return true; }
}
