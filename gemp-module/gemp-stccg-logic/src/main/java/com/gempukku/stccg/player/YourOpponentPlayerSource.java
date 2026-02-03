package com.gempukku.stccg.player;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class YourOpponentPlayerSource implements PlayerSource {
    @Override
    public boolean isPlayer(String playerName, DefaultGame cardGame, ActionContext actionContext) {
        PhysicalCard thisCard = actionContext.card();
        String opponentName = cardGame.getOpponent(playerName);
        if (thisCard.isBeingEncountered(cardGame)) {
            return thisCard.isBeingEncounteredBy(opponentName, cardGame);
        } else if (thisCard.isInPlay()) {
            return thisCard.isControlledBy(opponentName);
        } else {
            return thisCard.isOwnedBy(opponentName);
        }
    }

    @Override
    public String getPlayerName(DefaultGame cardGame, ActionContext actionContext) {
        String yourName = actionContext.yourName();
        return cardGame.getOpponent(yourName);
    }
}