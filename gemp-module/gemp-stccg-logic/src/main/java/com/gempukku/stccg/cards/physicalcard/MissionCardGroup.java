package com.gempukku.stccg.cards.physicalcard;

import com.fasterxml.jackson.annotation.*;
import com.gempukku.stccg.common.filterable.Zone;

import java.util.LinkedList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIncludeProperties({ "cards" })
@JsonIdentityInfo(generator= ObjectIdGenerators.PropertyGenerator.class, property="playerId")
public class MissionCardGroup {
    private final Zone _zone;
    private final List<MissionCard> _cards = new LinkedList<>();

    public MissionCardGroup(Zone zone) {
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
}