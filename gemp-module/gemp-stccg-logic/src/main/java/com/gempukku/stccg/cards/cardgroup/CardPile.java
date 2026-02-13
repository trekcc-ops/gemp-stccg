package com.gempukku.stccg.cards.cardgroup;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

import java.util.Collections;

public class CardPile<T extends PhysicalCard> extends PhysicalCardGroup<T> {

    public void addCardToTop(T card) { _cards.add(card); }
    public void addCardToBottom(T card) { _cards.addFirst(card); }
    public T getTopCard() { return _cards.getLast(); }
    public T getBottomCard() { return _cards.getFirst(); }

    public void shuffle() {
        Collections.shuffle(_cards);
    }

    public boolean isEmpty() {
        return _cards.isEmpty();
    }

    public void removeCard(T card) {
        _cards.remove(card);
    }
}