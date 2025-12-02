package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

public abstract class ActivateTribblePowerAction extends ActionyAction {
    protected final PhysicalCard _performingCard;
    protected final Player _performingPlayer;

    public ActivateTribblePowerAction(TribblesGame cardGame, ActionContext actionContext, PhysicalCard performingCard)
            throws PlayerNotFoundException {
        super(cardGame, actionContext.getPerformingPlayerId(), ActionType.ACTIVATE_TRIBBLE_POWER);
        _performingCard = performingCard;
        _performingPlayer = cardGame.getPlayer(_performingPlayerId);
    }

    public ActivateTribblePowerAction(TribblesGame cardGame, ActionContext actionContext,
                                      PhysicalCard performingCard,
                                      Enum<?>[] progressNames) throws InvalidGameLogicException, PlayerNotFoundException {
        super(cardGame, actionContext.getPerformingPlayerId(), "Activate tribble power",
                ActionType.ACTIVATE_TRIBBLE_POWER, progressNames);
        _performingCard = performingCard;
        _performingPlayer = cardGame.getPlayer(_performingPlayerId);
    }


    public PhysicalCard getSource() {
        return _performingCard;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException {
        Action cost = getNextCost();
        if (cost != null)
            return cost;

        Action action = getNextAction();
        if (action == null) {
            ActionResult result =
                    new ActionResult(ActionResult.Type.ACTIVATE_TRIBBLE_POWER, this);
            saveResult(result);
        }
        return action;
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        boolean result = costsCanBePaid(cardGame);
        for (Action action : getActions()) {
            if (!action.canBeInitiated(cardGame)) {
                result = false;
            }
        }
        return result;
    }

}