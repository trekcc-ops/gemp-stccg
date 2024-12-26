package com.gempukku.stccg.actions;

import com.gempukku.stccg.actions.turn.StackActionEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.blueprints.effect.EffectBlueprint;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

import java.util.LinkedList;
import java.util.List;

public class SubAction implements Action {
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

    private final Action _action;
    private Effect _effect;

    public SubAction(Action action, DefaultGame game) {
        _performingPlayerId = action.getPerformingPlayerId();
        _actionType = action.getActionType();
        _actionId = game.getActionsEnvironment().getNextActionId();
        game.getActionsEnvironment().incrementActionId();
        _action = action;
    }


    public SubAction(Action action, Effect effect) {
        this(action, effect.getGame());
        _effect = effect;
        _effects.add(effect);
    }

    public SubAction(Action action, ActionContext context,
                     List<EffectBlueprint> costAppenders, List<EffectBlueprint> effectBlueprints) {
        this(action, context.getGame());

        for (EffectBlueprint costAppender : costAppenders) {
            costAppender.addEffectToAction(true, this, context);
        }
        for (EffectBlueprint effectBlueprint : effectBlueprints)
            effectBlueprint.addEffectToAction(false, this, context);
    }

    public ActionType getActionType() { return _actionType; }

    @Override
    public PhysicalCard getCardForActionSelection() {
        return _action.getCardForActionSelection();
    }

    @Override
    public int getActionId() {
        return _actionId;
    }

    @Override
    public void insertCost(DefaultGame cardGame, Action action) {
        Effect effect = new StackActionEffect(cardGame, action);
        _costs.addFirst(effect);
    }

    @Override
    public PhysicalCard getPerformingCard() {
        return _action.getPerformingCard();
    }

    @Override
    public Effect nextEffect(DefaultGame cardGame) {
        if (isCostFailed()) {
            return null;
        } else {
            Effect cost = getNextCost();
            if (cost != null)
                return cost;

            return getNextEffect();
        }
    }

    public Effect getEffect() { return _effect; }

    @Override
    public boolean canBeInitiated(DefaultGame cardGame) {
        boolean result = costsCanBePaid();
        if (_effect != null && !_effect.isPlayableInFull())
            result = false;
        return result;
    }

    @Override
    public void appendCost(DefaultGame cardGame, Action costAction) {
        _costs.add(new StackActionEffect(cardGame, costAction));
    }

    @Override
    public void appendEffect(DefaultGame cardGame, Action actionEffect) {
        _effects.add(new StackActionEffect(cardGame, actionEffect));
    }

    @Override
    public void insertEffect(DefaultGame cardGame, Action actionEffect) {
        Effect effect = new StackActionEffect(cardGame, actionEffect);
        _effects.addFirst(effect);
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
    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String getActionSelectionText(DefaultGame game) {
        if (text == null) {
            return _action.getActionSelectionText(game);
        }
        return text;
    }

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

    public String getCardActionPrefix() { return null; }


}