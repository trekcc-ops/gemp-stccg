package com.gempukku.stccg.gameevent;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.player.Player;

public class FlashCardInPlayGameEvent extends GameEvent {

    public FlashCardInPlayGameEvent(PhysicalCard card, Player performingPlayer) {
        super(Type.FLASH_CARD_IN_PLAY, card, performingPlayer);
    }

}