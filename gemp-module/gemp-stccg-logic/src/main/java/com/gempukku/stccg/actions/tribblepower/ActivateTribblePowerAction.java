package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.HashMap;
import java.util.Map;

public abstract class ActivateTribblePowerAction extends ActionWithSubActions {
    protected final PhysicalCard _performingCard;
    protected Map<String, Boolean> _progressIndicators = new HashMap<>();

    public ActivateTribblePowerAction(TribblesGame cardGame, ActionContext actionContext, PhysicalCard performingCard) {
        super(cardGame, actionContext.getPerformingPlayerId(), ActionType.ACTIVATE_TRIBBLE_POWER);
        _performingCard = performingCard;
    }

    public ActivateTribblePowerAction(TribblesGame cardGame, ActionContext actionContext,
                                      PhysicalCard performingCard,
                                      Enum<?>[] progressNames) throws InvalidGameLogicException, PlayerNotFoundException {
        super(cardGame, actionContext.getPerformingPlayerId(), ActionType.ACTIVATE_TRIBBLE_POWER, actionContext);
        _performingCard = performingCard;
        for (Enum<?> progressType : progressNames) {
            _progressIndicators.put(progressType.name(), false);
        }
    }


    public PhysicalCard getSource() {
        return _performingCard;
    }

    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException {
        Action cost = getNextCost();
        if (cost != null)
            return cost;

        Action action = getNextAction();
        if (action == null) {
            ActionResult result =
                    new ActionResult(ActionResult.Type.ACTIVATE_TRIBBLE_POWER, this);
            saveResult(result, cardGame);
        }
        return action;
    }

    protected final void processEffect(DefaultGame cardGame) {
        try {
            Action action = nextAction(cardGame);
            if (action == null) {
                setAsSuccessful();
            }
        } catch(InvalidGameLogicException | PlayerNotFoundException exp) {
            cardGame.sendErrorMessage(exp);
            setAsFailed();
        }
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        boolean result = costsCanBePaid(cardGame);
        for (Action action : _actionEffects) {
            if (!action.canBeInitiated(cardGame)) {
                result = false;
            }
        }
        return result;
    }

    protected boolean getProgress(Enum<?> progressType) {
        return _progressIndicators.get(progressType.name());
    }

    protected void setProgress(Enum<?> progressType) {
        _progressIndicators.put(progressType.name(), true);
    }

    protected final Action getNextAction() {
        final Action effect = _actionEffects.poll();
        if (effect != null)
            _processedActions.add(effect);
        return effect;
    }

    protected final Action getNextCost() {
        Action cost = _costs.poll();
        if (cost != null)
            _processedCosts.add(cost);
        return cost;
    }

    public PhysicalCard getPerformingCard() {
        return _performingCard;
    }

}