package com.gempukku.stccg.player;

import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class YourOpponentPlayerSource implements PlayerSource {
    @Override
    public boolean isPlayer(String playerName, DefaultGame cardGame, GameTextContext actionContext) {
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
    public String getPlayerName(DefaultGame cardGame, GameTextContext actionContext) {
        String yourName = actionContext.yourName();
        return cardGame.getOpponent(yourName);
    }
}