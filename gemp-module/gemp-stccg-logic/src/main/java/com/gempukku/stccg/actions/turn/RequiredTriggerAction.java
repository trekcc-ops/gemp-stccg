package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.PlayerNotFoundException;

public class RequiredTriggerAction extends ActionyAction implements TopLevelSelectableAction {
    private final PhysicalCard _performingCard;
    private enum Progress { sentMessage }

    public RequiredTriggerAction(PhysicalCard physicalCard) {
        super(physicalCard.getGame(), physicalCard.getOwner(), "Required trigger from " + physicalCard.getCardLink(),
                ActionType.USE_GAME_TEXT, Progress.values());
        _performingCard = physicalCard;
    }

    @Override
    public PhysicalCard getPerformingCard() {
        return _performingCard;
    }

    @Override
    public int getCardIdForActionSelection() {
        return _performingCard.getCardId();
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws PlayerNotFoundException {
        if (!getProgress(Progress.sentMessage)) {
            setProgress(Progress.sentMessage);
            if (_performingCard != null) {
                cardGame.sendMessage(_performingCard.getCardLink() + " required triggered effect is used");
            }
        }

        if (isCostFailed()) {
            return null;
        } else {
            Action cost = getNextCost();
            if (cost != null)
                return cost;

            return getNextAction();
        }
    }

    public boolean requirementsAreMet(DefaultGame cardGame) { return true; }
}