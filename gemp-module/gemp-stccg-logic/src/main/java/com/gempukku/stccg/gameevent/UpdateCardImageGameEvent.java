package com.gempukku.stccg.gameevent;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

public class UpdateCardImageGameEvent extends GameEvent {

    private final PhysicalCard _card;

    public UpdateCardImageGameEvent(PhysicalCard card) {
        super(GameEvent.Type.UPDATE_CARD_IMAGE, card.getOwner());
        _card = card;
        _eventAttributes.put(Attribute.cardId, String.valueOf(_card.getCardId()));
        _eventAttributes.put(Attribute.imageUrl, String.valueOf(_card.getImageUrl()));
    }

    @JsonProperty("cardId")
    private int getCardId() {
        return _card.getCardId();
    }

    @JsonProperty("imageUrl")
    private String getImageUrl() {
        return _card.getImageUrl();
    }
}