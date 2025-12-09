package com.gempukku.stccg.player;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.game.DefaultGame;

public class OpponentPlayerSource implements PlayerSource {
    @Override
    public String getPlayerId(DefaultGame cardGame, ActionContext actionContext) {
        String cardOwnerName = actionContext.card().getOwnerName();
        return cardGame.getOpponent(cardOwnerName);
    }
}