package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class PassThroughEffectAction extends ActionyAction {

    private final PhysicalCard _card;

    public PassThroughEffectAction(PhysicalCard source, Effect effect) {
        super(source.getGame());
        appendEffect(effect);
        _card = source;
    }

    public boolean requirementsAreMet(DefaultGame cardGame) { return true; }

    @Override
    public PhysicalCard getActionSource() {
        return _card;
    }

    @Override
    public PhysicalCard getCardForActionSelection() {
        return _card;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) {
        if (isCostFailed()) {
            return null;
        } else {
            Action cost = getNextCost();
            if (cost != null)
                return cost;

            return getNextAction();
        }
    }
}