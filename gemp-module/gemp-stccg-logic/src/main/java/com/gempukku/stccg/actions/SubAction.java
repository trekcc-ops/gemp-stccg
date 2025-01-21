package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.blueprints.effect.EffectBlueprint;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

import java.util.List;

public class SubAction extends ActionyAction implements CardPerformedAction {

    private final CardPerformedAction _parentAction;

    public SubAction(CardPerformedAction action, ActionContext context) {
        super(context.getGame(), context.getGame().getPlayer(action.getPerformingPlayerId()), action.getActionType());
        _parentAction = action;
    }

    public SubAction(CardPerformedAction action, ActionContext context,
                     List<EffectBlueprint> costAppenders, List<EffectBlueprint> effectBlueprints) {
        this(action, context);

        for (EffectBlueprint costAppender : costAppenders) {
            costAppender.addEffectToAction(true, this, context);
        }
        for (EffectBlueprint effectBlueprint : effectBlueprints)
            effectBlueprint.addEffectToAction(false, this, context);
    }

    @Override
    public boolean canBeInitiated(DefaultGame cardGame) {
        return costsCanBePaid(cardGame);
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    public String getCardActionPrefix() { return null; }

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


    @Override
    public PhysicalCard getPerformingCard() {
        return _parentAction.getPerformingCard();
    }
}