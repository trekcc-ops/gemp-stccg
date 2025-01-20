package com.gempukku.stccg.actions.choose;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

import java.util.Collection;

public class MakeDecisionAction extends ActionyAction {
    private final AwaitingDecision _decision;

    public MakeDecisionAction(PhysicalCard performingCard, AwaitingDecision decision) {
        super(decision.getDecidingPlayer(performingCard.getGame()), decision.getText(), ActionType.MAKE_DECISION);
        _decision = decision;
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        cardGame.getUserFeedback().sendAwaitingDecision(_decision);
        return getNextAction();
    }

}