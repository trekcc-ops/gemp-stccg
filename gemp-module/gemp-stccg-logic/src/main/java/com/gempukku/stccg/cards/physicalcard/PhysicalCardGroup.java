package com.gempukku.stccg.cards.physicalcard;

import com.fasterxml.jackson.annotation.*;
import com.gempukku.stccg.common.filterable.Zone;

import java.util.LinkedList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIncludeProperties({ "cards" })
@JsonIdentityInfo(generator= ObjectIdGenerators.PropertyGenerator.class, property="playerId")
public class PhysicalCardGroup {
    private final Zone _zone;
    private final List<PhysicalCard> _cards = new LinkedList<>();

    public PhysicalCardGroup(Zone zone) {
        _zone = zone;
    }

    public void addCard(PhysicalCard card) { _cards.add(card); }

    @JsonIdentityReference(alwaysAsId=true)
    public List<PhysicalCard> getCards() {
        return _cards;
    }
    public void setCards(List<PhysicalCard> subDeck) {
        _cards.clear();
        _cards.addAll(subDeck);
    }

    public int size() {
        return _cards.size();
    }

    public PhysicalCard getFirst() {
        return _cards.getFirst();
    }
}