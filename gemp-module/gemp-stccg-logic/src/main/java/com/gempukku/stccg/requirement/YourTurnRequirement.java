package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.game.DefaultGame;

public class YourTurnRequirement implements Requirement {

    public boolean accepts(ActionContext actionContext, DefaultGame cardGame) {
        String playerName = actionContext.yourName();
        return cardGame.isPlayersTurn(playerName);
    }
}