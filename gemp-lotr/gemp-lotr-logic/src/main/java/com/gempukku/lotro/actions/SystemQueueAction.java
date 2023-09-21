package com.gempukku.lotro.actions;

import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.effects.Effect;

public class SystemQueueAction extends AbstractCostToEffectAction {
    @Override
    public Type getType() {
        return Type.OTHER;
    }

    @Override
    public LotroPhysicalCard getActionSource() {
        return null;
    }

    @Override
    public LotroPhysicalCard getActionAttachedToCard() {
        return null;
    }

    @Override
    public Effect nextEffect(DefaultGame game) {
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
