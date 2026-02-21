package com.gempukku.stccg.actions;

import com.gempukku.stccg.actions.blueprints.SubActionBlueprint;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

import java.util.List;

public class SubAction extends ActionWithSubActions implements CardPerformedAction {

    private final ActionWithSubActions _parentAction;

    public SubAction(DefaultGame cardGame, ActionWithSubActions action, GameTextContext context,
                     List<SubActionBlueprint> costAppenders, List<SubActionBlueprint> subActionBlueprints) {
        super(cardGame, action.getPerformingPlayerId(), action.getActionType());
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

    @Override
    public PhysicalCard getPerformingCard() {
        return _parentAction.getPerformingCard();
    }
}