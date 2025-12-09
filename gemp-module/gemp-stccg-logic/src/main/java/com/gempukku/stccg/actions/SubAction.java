package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.actions.blueprints.SubActionBlueprint;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.List;

public class SubAction extends ActionyAction implements CardPerformedAction {

    private final CardPerformedAction _parentAction;

    public SubAction(DefaultGame cardGame, CardPerformedAction action, ActionContext context,
                     List<SubActionBlueprint> costAppenders, List<SubActionBlueprint> subActionBlueprints) throws PlayerNotFoundException {
        super(cardGame, cardGame.getPlayer(action.getPerformingPlayerId()), action.getActionType());
        _parentAction = action;

        for (SubActionBlueprint costAppender : costAppenders) {
            costAppender.addEffectToAction(cardGame, true, this, context);
        }
        for (SubActionBlueprint subActionBlueprint : subActionBlueprints)
            subActionBlueprint.addEffectToAction(cardGame, false, this, context);
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