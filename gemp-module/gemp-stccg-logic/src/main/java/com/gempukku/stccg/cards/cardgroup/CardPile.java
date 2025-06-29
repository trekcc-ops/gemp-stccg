package com.gempukku.stccg.cards.cardgroup;

import com.fasterxml.jackson.annotation.*;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class CardPile extends PhysicalCardGroup {

    public void addCardToTop(PhysicalCard card) { _cards.add(card); }
    public void addCardToBottom(PhysicalCard card) { _cards.addFirst(card); }
    public PhysicalCard getTopCard() { return _cards.getLast(); }
    public PhysicalCard getBottomCard() { return _cards.getFirst(); }

    public void shuffle() {
        Collections.shuffle(_cards);
    }
    public boolean contains(PhysicalCard card) { return _cards.contains(card); }

    public boolean isEmpty() {
        return _cards.isEmpty();
    }

    public void removeCard(PhysicalCard card) {
        _cards.remove(card);
    }
}