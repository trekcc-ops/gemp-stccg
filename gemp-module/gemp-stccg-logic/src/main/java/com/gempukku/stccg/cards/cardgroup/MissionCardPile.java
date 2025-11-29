package com.gempukku.stccg.cards.cardgroup;

import com.fasterxml.jackson.annotation.*;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;

import java.util.LinkedList;
import java.util.List;

public class MissionCardPile {
    private final List<MissionCard> _cards = new LinkedList<>();

    public void addCard(MissionCard card) { _cards.add(card); }

    public List<MissionCard> getCards() {
        return _cards;
    }

    public PhysicalCard getBottomCard() { return _cards.getFirst(); }

    public int size() { return _cards.size(); }
}