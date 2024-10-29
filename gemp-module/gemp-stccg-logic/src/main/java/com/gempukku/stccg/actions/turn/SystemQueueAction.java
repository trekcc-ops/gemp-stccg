package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.AbstractCostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class SystemQueueAction extends AbstractCostToEffectAction {
    protected final DefaultGame _game;
    public SystemQueueAction(DefaultGame game) {
        _game = game;
    }

    @Override
    public DefaultGame getGame() { return _game; }
    @Override
    public PhysicalCard getActionSource() {
        return null;
    }

    @Override
    public PhysicalCard getCardForActionSelection() {
        return null;
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