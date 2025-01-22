package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.common.filterable.Zone;

import java.util.LinkedList;
import java.util.List;

public class CardGroup {
    private final Zone _zone;
    private final List<PhysicalCard> _cards = new LinkedList<>();

    public CardGroup(Zone zone) {
        _zone = zone;
    }

    public void addCard(PhysicalCard card) { _cards.add(card); }

    public List<? extends PhysicalCard> getCards() {
        return _cards;
    }
}