package com.gempukku.stccg.cards;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.requirement.Requirement;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.util.*;

public class GameTextContext {
    private final String _performingPlayerName;
    private final Multimap<String, Integer> _cardMemoryNew = HashMultimap.create();
    private final Map<String, String> _valueMemory = new HashMap<>();
    private final PhysicalCard _thisCard;

    public GameTextContext(PhysicalCard thisCard, String yourName) {
        _performingPlayerName = yourName;
        _thisCard = thisCard;
    }

    public String getPerformingPlayerId() { return _performingPlayerName; }


    public void setValueToMemory(String memory, String value) {
        if(memory != null) {
            memory = memory.toLowerCase();
        }
        _valueMemory.put(memory, value);
    }


    public String getValueFromMemory(String memory) {
        if(memory != null) {
            memory = memory.toLowerCase();
        }
        final String result = _valueMemory.get(memory);
        if (result == null)
            throw new IllegalArgumentException("Memory not found - " + memory);
        return result;
    }

    public void setCardMemory(String memory, PhysicalCard card) {
        if(memory != null) {
            memory = memory.toLowerCase();
        }
        _cardMemoryNew.removeAll(memory);
        if (card != null)
            _cardMemoryNew.put(memory, card.getCardId());
    }


    public void setCardMemory(String memory, Collection<? extends PhysicalCard> cards) {
        if(memory != null) {
            memory = memory.toLowerCase();
        }
        Collection<Integer> cardIds = new ArrayList<>();
        for (PhysicalCard card : cards) {
            cardIds.add(card.getCardId());
        }
        _cardMemoryNew.removeAll(memory);
        _cardMemoryNew.putAll(memory, cardIds);
    }

    public Collection<Integer> getCardIdsFromMemory(String memory) {
        if (memory == null) {
            return new ArrayList<>();
        } else {
            memory = memory.toLowerCase();
            if (memory.equals("thispersonnel")) {
                if (_thisCard.getCardType() == CardType.PERSONNEL) {
                    return List.of(_thisCard.getCardId());
                } else if (_thisCard.getAtopCard() != null && _thisCard.getAtopCard().getCardType() == CardType.PERSONNEL) {
                    return List.of(_thisCard.getAtopCard().getCardId());
                }
            }
            return Objects.requireNonNullElse(_cardMemoryNew.get(memory), new ArrayList<>());
        }
    }

    public boolean acceptsAllRequirements(DefaultGame cardGame, Iterable<Requirement> requirements) {
        if (requirements == null)
            return true;
        boolean result = true;
        for (Requirement requirement : requirements) {
            if (!requirement.accepts(this, cardGame)) result = false;
        }
        return result;
    }

    public PhysicalCard card() {
        return _thisCard;
    }

    public String yourName() {
        return _performingPlayerName;
    }

}