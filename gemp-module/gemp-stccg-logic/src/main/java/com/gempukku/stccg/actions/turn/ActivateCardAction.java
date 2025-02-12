package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

public class ActivateCardAction extends ActionyAction implements TopLevelSelectableAction {

    private final PhysicalCard _performingCard;
    private enum Progress { sentMessage, activated, prevented }

    public ActivateCardAction(DefaultGame cardGame, PhysicalCard physicalCard) {
        super(cardGame, physicalCard.getOwner(), "Use " + physicalCard.getFullName(), ActionType.USE_GAME_TEXT,
                Progress.values());
        _performingCard = physicalCard;
    }

    @Override
    public PhysicalCard getPerformingCard() {
        return _performingCard;
    }

    // TODO - Not sure this is accurate. Also not sure we need this class at all.
    public boolean requirementsAreMet(DefaultGame game) { return true; }

    @Override
    public int getCardIdForActionSelection() {
        return _performingCard.getCardId();
    }


    public void prevent() {
        setProgress(Progress.prevented);
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws PlayerNotFoundException {
        if (!getProgress(Progress.sentMessage)) {
            setProgress(Progress.sentMessage);
            if (_performingCard != null && _performingCard.isInPlay()) {
                Player performingPlayer = cardGame.getPlayer(_performingPlayerId);
                cardGame.activatedCard(performingPlayer, _performingCard);
                cardGame.sendMessage(_performingCard.getCardLink() + " is used");
            }
        }

        if (!isCostFailed()) {
            Action cost = getNextCost();
            if (cost != null)
                return cost;

            if (!getProgress(Progress.activated)) {
                setProgress(Progress.activated);
                cardGame.getActionsEnvironment().emitEffectResult(
                        new ActionResult(ActionResult.Type.ACTIVATE, this, _performingCard));
            }

            if (!getProgress(Progress.prevented))
                return getNextAction();
        }
        return null;
    }

}