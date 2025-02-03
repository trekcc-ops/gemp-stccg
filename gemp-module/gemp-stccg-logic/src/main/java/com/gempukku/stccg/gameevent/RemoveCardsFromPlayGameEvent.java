package com.gempukku.stccg.gameevent;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.player.Player;

import java.util.Set;
import java.util.StringJoiner;

public class RemoveCardsFromPlayGameEvent extends GameEvent {

    @JacksonXmlProperty(localName = "otherCardIds", isAttribute = true)
    private String _otherCardIds;

    public RemoveCardsFromPlayGameEvent(Set<PhysicalCard> visibleRemovedCards, Player performingPlayer) {
        super(GameEvent.Type.REMOVE_CARD_FROM_PLAY, performingPlayer);
        StringJoiner sj = new StringJoiner(",");
        for (PhysicalCard card : visibleRemovedCards) {
            sj.add(String.valueOf(card.getCardId()));
        }
        _otherCardIds = sj.toString();
        _eventAttributes.put(Attribute.otherCardIds, sj.toString());
    }

}