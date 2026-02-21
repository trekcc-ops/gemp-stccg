package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.game.DefaultGame;

public class YourTurnRequirement implements Requirement {

    public boolean accepts(GameTextContext actionContext, DefaultGame cardGame) {
        String playerName = actionContext.yourName();
        return cardGame.isPlayersTurn(playerName);
    }
}