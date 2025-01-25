package com.gempukku.stccg.cards.cardgroup;

import com.fasterxml.jackson.annotation.*;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class CardPile extends PhysicalCardGroup {
    private final List<PhysicalCard> _cards = new LinkedList<>();

    public CardPile() {
    }

    public void addCard(PhysicalCard<? extends DefaultGame> card) { _cards.add(card); }

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

    public void addCardToTop(PhysicalCard card) { _cards.add(card); }
    public void addCardToBottom(PhysicalCard card) { _cards.addFirst(card); }
    public PhysicalCard getTopCard() { return _cards.getFirst(); }
    public PhysicalCard getBottomCard() { return _cards.getLast(); }

    public void addCards(Collection<PhysicalCard> cards) { _cards.addAll(cards); }

    public void shuffle() {
        Collections.shuffle(_cards);
    }

    public boolean isEmpty() {
        return _cards.isEmpty();
    }

    public void removeCard(PhysicalCard card) {
        _cards.remove(card);
    }
}