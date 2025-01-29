package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.blueprints.effect.SubActionBlueprint;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.PlayerNotFoundException;

import java.util.List;

public class SubAction extends ActionyAction implements CardPerformedAction {

    private final CardPerformedAction _parentAction;

    public SubAction(CardPerformedAction action, ActionContext context) throws PlayerNotFoundException {
        super(context.getGame(), context.getGame().getPlayer(action.getPerformingPlayerId()), action.getActionType());
        _parentAction = action;
    }

    public SubAction(CardPerformedAction action, ActionContext context,
                     List<SubActionBlueprint> costAppenders, List<SubActionBlueprint> subActionBlueprints) throws PlayerNotFoundException {
        this(action, context);

        for (SubActionBlueprint costAppender : costAppenders) {
            costAppender.addEffectToAction(true, this, context);
        }
        for (SubActionBlueprint subActionBlueprint : subActionBlueprints)
            subActionBlueprint.addEffectToAction(false, this, context);
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