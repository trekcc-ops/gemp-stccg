package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.TribblePower;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.PlayerNotFoundException;

public abstract class ActivateTribblePowerAction extends ActionyAction {
    protected final PhysicalCard _performingCard;
    protected final TribblePower _tribblePower;
    protected final Player _performingPlayer;

    public ActivateTribblePowerAction(TribblesActionContext actionContext, TribblePower power)
            throws PlayerNotFoundException {
        super(actionContext.getGame(), actionContext.getPerformingPlayer(), ActionType.ACTIVATE_TRIBBLE_POWER);
        _performingCard = actionContext.getSource();
        _tribblePower = power;
        _performingPlayer = actionContext.getGame().getPlayer(_performingPlayerId);
    }

    public ActivateTribblePowerAction(TribblesActionContext actionContext, TribblePower power,
                                      Enum<?>[] progressNames) throws InvalidGameLogicException, PlayerNotFoundException {
        super(actionContext.getGame(), actionContext.getPerformingPlayer(), "Activate tribble power",
                ActionType.ACTIVATE_TRIBBLE_POWER, progressNames);
        _performingCard = actionContext.getSource();
        _tribblePower = power;
        _performingPlayer = actionContext.getGame().getPlayer(_performingPlayerId);
    }


    @Override
    public String getActionSelectionText(DefaultGame cardGame) {
        return "Activate " + _performingCard.getCardLink();
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
                    new ActionResult(ActionResult.Type.ACTIVATE_TRIBBLE_POWER, this, _performingCard);
            cardGame.getActionsEnvironment().emitEffectResult(result);
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