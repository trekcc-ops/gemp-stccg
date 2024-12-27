package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

public class MakeDecisionAction extends ActionyAction {
    private final AwaitingDecision _decision;
    private final PhysicalCard _performingCard;

    public MakeDecisionAction(PhysicalCard performingCard, AwaitingDecision decision) {
        super(decision.getDecidingPlayer(performingCard.getGame()), decision.getText(), ActionType.MAKE_DECISION);
        _decision = decision;
        _performingCard = performingCard;
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

    @Override
    public PhysicalCard getPerformingCard() {
        return _performingCard;
    }

    @Override
    public PhysicalCard getCardForActionSelection() {
        return _performingCard;
    }

}