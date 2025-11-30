package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.game.DefaultGame;

public class CardIsInHandRequirement implements Requirement {

    @Override
    public boolean accepts(ActionContext actionContext, DefaultGame cardGame) {
        return actionContext.getSource().isInHand(cardGame);
    }
}