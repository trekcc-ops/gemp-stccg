package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.effects.Effect;

public class SystemQueueAction extends AbstractCostToEffectAction {
    private final DefaultGame _game;
    public SystemQueueAction(DefaultGame game) {
        _game = game;
    }

    @Override
    public DefaultGame getGame() { return _game; }
    @Override
    public ActionType getActionType() {
        return ActionType.OTHER;
    }

    @Override
    public PhysicalCard getActionSource() {
        return null;
    }

    @Override
    public PhysicalCard getActionAttachedToCard() {
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
