package com.gempukku.stccg.cards.cardgroup;

import com.fasterxml.jackson.annotation.*;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;

import java.util.LinkedList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIncludeProperties({ "cards" })
@JsonIdentityInfo(generator= ObjectIdGenerators.PropertyGenerator.class, property="playerId")
public class MissionCardPile {
    private final Zone _zone;
    private final List<MissionCard> _cards = new LinkedList<>();

    public MissionCardPile(Zone zone) {
        _zone = zone;
    }

    public void addCard(MissionCard card) { _cards.add(card); }

    @JsonIdentityReference(alwaysAsId=true)
    public List<MissionCard> getCards() {
        return _cards;
    }
    public void setCards(List<MissionCard> cards) {
        _cards.clear();
        _cards.addAll(cards);
    }

    public PhysicalCard getBottomCard() { return _cards.getFirst(); }
}