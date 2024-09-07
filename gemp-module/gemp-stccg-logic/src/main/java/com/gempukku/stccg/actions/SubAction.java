package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class SubAction extends AbstractCostToEffectAction {
    private final Action _action;
    protected final DefaultGame _game;

    public SubAction(Action action) {
        super(action);
        _action = action;
        _game = action.getGame();
    }

    @Override
    public DefaultGame getGame() { return _game; }

    @Override
    public PhysicalCard getActionAttachedToCard() {
        return _action.getActionAttachedToCard();
    }

    @Override
    public PhysicalCard getActionSource() {
        return _action.getActionSource();
    }

    @Override
    public String getPerformingPlayerId() {
        return _action.getPerformingPlayerId();
    }

    @Override
    public String getText() {
        return _action.getText();
    }

    @Override
    public Effect nextEffect() {
        if (isCostFailed()) {
            return null;
        } else {
            Effect cost = getNextCost();
            if (cost != null)
                return cost;

            return getNextEffect();
        }
    }
}
