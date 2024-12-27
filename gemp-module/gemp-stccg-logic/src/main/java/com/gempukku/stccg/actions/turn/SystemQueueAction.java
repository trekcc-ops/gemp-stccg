package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

public class SystemQueueAction extends ActionyAction {

    public SystemQueueAction(DefaultGame game) {
        super(game);
    }

    public boolean requirementsAreMet(DefaultGame cardGame) { return true; }

    @Override
    public PhysicalCard getPerformingCard() {
        return null;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
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