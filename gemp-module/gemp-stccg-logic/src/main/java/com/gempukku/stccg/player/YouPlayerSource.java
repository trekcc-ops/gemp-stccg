package com.gempukku.stccg.player;

import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class YouPlayerSource implements PlayerSource {
    @Override
    public boolean isPlayer(String playerName, DefaultGame cardGame, GameTextContext actionContext) {
        PhysicalCard thisCard = actionContext.card();

        if (thisCard.isBeingEncountered(cardGame)) {
            return thisCard.isBeingEncounteredBy(playerName, cardGame);
        } else if (thisCard.isInPlay()) {
            return thisCard.isControlledBy(playerName);
        } else {
            return thisCard.isOwnedBy(playerName);
        }
    }
    @Override
    public String getPlayerName(DefaultGame cardGame, GameTextContext actionContext) {
        PhysicalCard thisCard = actionContext.card();
        return thisCard.getOwnerName();
    }
}