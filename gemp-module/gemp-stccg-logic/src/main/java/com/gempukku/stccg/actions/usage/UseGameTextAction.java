package com.gempukku.stccg.actions.usage;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;

public class UseGameTextAction extends ActionyAction implements TopLevelSelectableAction {
    private final PhysicalCard _performingCard;

    public UseGameTextAction(PhysicalCard physicalCard, Player performingPlayer, String text) {
        super(physicalCard.getGame(), performingPlayer, text, ActionType.USE_GAME_TEXT);
        _performingCard = physicalCard;
    }


    @Override
    public PhysicalCard getPerformingCard() {
        return _performingCard;
    }

    public boolean requirementsAreMet(DefaultGame game) { return true; }

    @Override
    public int getCardIdForActionSelection() {
        return _performingCard.getCardId();
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