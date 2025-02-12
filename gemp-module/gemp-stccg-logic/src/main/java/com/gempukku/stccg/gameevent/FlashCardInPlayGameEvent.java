package com.gempukku.stccg.gameevent;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.player.Player;

public class FlashCardInPlayGameEvent extends GameEvent {

    @JsonProperty("cardId")
    private final int _cardId;

    public FlashCardInPlayGameEvent(PhysicalCard card, Player performingPlayer) {
        super(Type.FLASH_CARD_IN_PLAY, performingPlayer);
        _cardId = card.getCardId();
        _eventAttributes.put(Attribute.cardId, String.valueOf(_cardId));
    }

}