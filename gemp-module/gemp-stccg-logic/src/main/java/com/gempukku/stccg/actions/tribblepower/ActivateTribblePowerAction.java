package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.TribblePower;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

import java.util.Collection;

public abstract class ActivateTribblePowerAction extends ActionyAction {
    protected final PhysicalCard _performingCard;
    protected final TribblePower _tribblePower;

    public ActivateTribblePowerAction(TribblesActionContext actionContext, TribblePower power) {
        super(actionContext.getPerformingPlayer(), ActionType.ACTIVATE_TRIBBLE_POWER);
        _performingCard = actionContext.getSource();
        _tribblePower = power;
    }

    public ActivateTribblePowerAction(TribblesActionContext actionContext, TribblePower power,
                                      Enum<?>[] progressNames) throws InvalidGameLogicException {
        super(actionContext.getPerformingPlayer(), "Activate tribble power", ActionType.ACTIVATE_TRIBBLE_POWER, progressNames);
        _performingCard = actionContext.getSource();
        _tribblePower = power;
    }


    @Override
    public String getActionSelectionText(DefaultGame cardGame) {
        return "Activate " + _performingCard.getCardLink();
    }

    public PhysicalCard getSource() {
        return _performingCard;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        Action cost = getNextCost();
        if (cost != null)
            return cost;

        Action action = getNextAction();
        if (action == null) {
            EffectResult result =
                    new EffectResult(EffectResult.Type.ACTIVATE_TRIBBLE_POWER, this, _performingCard);
            cardGame.getActionsEnvironment().emitEffectResult(result);
        }
        return action;
    }

    @Override
    public PhysicalCard getPerformingCard() {
        return _performingCard;
    }

    @Override
    public PhysicalCard getCardForActionSelection() {
        return _performingCard;
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