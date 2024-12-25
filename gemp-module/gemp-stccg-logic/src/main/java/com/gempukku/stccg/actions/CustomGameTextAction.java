package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;

public class CustomGameTextAction extends ActionyAction {
    private final PhysicalCard _physicalCard;

    public CustomGameTextAction(PhysicalCard physicalCard, Player performingPlayer, String text) {
        super(performingPlayer, text, ActionType.USE_GAME_TEXT);
        _physicalCard = physicalCard;
    }


    @Override
    public PhysicalCard getPerformingCard() {
        return _physicalCard;
    }

    public boolean requirementsAreMet(DefaultGame game) { return true; }

    @Override
    public PhysicalCard getCardForActionSelection() {
        return _physicalCard;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) {
        if (!isCostFailed()) {
            Action cost = getNextCost();
            if (cost != null)
                return cost;
        }
        return getNextAction();
    }

}