package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

public class CardIsInHandRequirement implements Requirement {

    @Override
    public boolean accepts(ActionContext actionContext, DefaultGame cardGame) {
        try {
            return actionContext.getPerformingCard(cardGame).isInHand(cardGame);
        } catch(InvalidGameLogicException exp) {
            cardGame.sendErrorMessage(exp);
            return false;
        }
    }
}