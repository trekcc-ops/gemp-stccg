package com.gempukku.stccg.gameevent;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class UpdateCardImageGameEvent extends GameEvent {

    public UpdateCardImageGameEvent(DefaultGame cardGame, PhysicalCard card) {
        super(cardGame, GameEvent.Type.UPDATE_CARD_IMAGE, card);
    }
}