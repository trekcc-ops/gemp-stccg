package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class SubAction extends AbstractCostToEffectAction {
    private final Action _action;

    public SubAction(Action action) {
        super(action);
        _action = action;
    }

    @Override
    public PhysicalCard getCardForActionSelection() {
        return _action.getCardForActionSelection();
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
    public String getText(DefaultGame game) {
        return _action.getText(game);
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
}