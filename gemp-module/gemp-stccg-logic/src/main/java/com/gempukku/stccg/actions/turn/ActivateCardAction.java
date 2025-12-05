package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.PlayerNotFoundException;

public class ActivateCardAction extends ActionyAction implements TopLevelSelectableAction {

    private final PhysicalCard _performingCard;
    private enum Progress {activated, prevented }

    public ActivateCardAction(DefaultGame cardGame, PhysicalCard physicalCard, ActionContext context) {
        super(cardGame, physicalCard.getOwnerName(), ActionType.USE_GAME_TEXT,
                Progress.values(), context);
        _performingCard = physicalCard;
    }

    @Override
    public PhysicalCard getPerformingCard() {
        return _performingCard;
    }

    // TODO - Not sure this is accurate. Also not sure we need this class at all.
    public boolean requirementsAreMet(DefaultGame game) { return true; }


    public void prevent() {
        setProgress(Progress.prevented);
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws PlayerNotFoundException {
        if (!isCostFailed()) {
            Action cost = getNextCost();
            if (cost != null)
                return cost;

            if (!getProgress(Progress.activated)) {
                setProgress(Progress.activated);
                saveResult(new ActionResult(ActionResult.Type.ACTIVATE, this));
            }

            if (!getProgress(Progress.prevented)) {
                Action nextAction = getNextAction();
                if (nextAction != null) {
                    return nextAction;
                } else {
                    setAsSuccessful();
                }
            } else {
                setAsFailed();
            }
        } else {
            setAsFailed();
        }
        return null;
    }

}