package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class SubAction extends AbstractCostToEffectAction {
    private final Action _action;
    private Effect _effect;

    public SubAction(Action action) {
        super(action);
        _action = action;
    }

    public SubAction(Action action, DefaultGame game) {
        super(game, action);
        _action = action;
    }


    public SubAction(Action action, Effect effect) {
        super(effect.getGame(), action);
        _action = action;
        _effect = effect;
        appendEffect(effect);
    }

    @Override
    public PhysicalCard getCardForActionSelection() {
        return _action.getCardForActionSelection();
    }

    @Override
    public int getActionId() {
        return _actionId;
    }

    @Override
    public PhysicalCard getPerformingCard() {
        return _action.getPerformingCard();
    }

    @Override
    public String getPerformingPlayerId() {
        return _action.getPerformingPlayerId();
    }

    @Override
    public String getActionSelectionText(DefaultGame game) {
        return _action.getActionSelectionText(game);
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
}