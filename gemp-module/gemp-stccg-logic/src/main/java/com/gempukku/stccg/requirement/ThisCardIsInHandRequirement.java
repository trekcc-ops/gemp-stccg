package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.game.DefaultGame;

public class ThisCardIsInHandRequirement implements Requirement {

    public boolean accepts(GameTextContext actionContext, DefaultGame cardGame) {
        return actionContext.card().isInHand(cardGame);
    }

}