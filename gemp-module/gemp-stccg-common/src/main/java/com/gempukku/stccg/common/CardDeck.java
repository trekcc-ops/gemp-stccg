package com.gempukku.stccg.common;

import com.gempukku.stccg.common.filterable.SubDeck;

import java.util.*;

public class CardDeck {
    Map<SubDeck, List<String>> _subDecks = new HashMap<>();
    protected final String _deckName;
    protected String _notes;
    protected String _targetFormat;
    public CardDeck(String deckName) {
        _deckName = deckName;
    }
    public CardDeck(CardDeck deck) {
        _deckName = deck.getDeckName();
        _subDecks = deck.getSubDecks();
        _notes = deck.getNotes();
        _targetFormat = deck.getTargetFormat();
    }

    public CardDeck(String deckName, String contents, String targetFormat, String notes) {
        _deckName = deckName;
        _targetFormat = targetFormat;
        _notes = notes;
        parseContents(contents);
    }

    public CardDeck(String deckName, String contents, String targetFormat) {
        _deckName = deckName;
        _targetFormat = targetFormat;
        _notes = "";
        parseContents(contents);
    }


    public void parseContents(String contents) {
        String[] parts = contents.split("\\|");
        for (int i = 0; i < parts.length; i += 2) {
            List<String> cards = new ArrayList<>();
            for (String card : parts[i+1].split(",")) {
                if (!card.isEmpty()) {
                    cards.add(card);
                }
            }
            _subDecks.put(SubDeck.valueOf(parts[i]), cards);
        }
    }

    public String buildContentsFromDeck() {
        Collection<String> parts = new ArrayList<>();
        _subDecks.forEach((k, v) -> {
            parts.add(k.name());
            parts.add(String.join(",",v));
        });
        return String.join("|", parts);
    }

    public String getDeckName() {
        return _deckName;
    }
    public void addCard(String blueprintId) { addCard(SubDeck.DRAW_DECK, blueprintId); }
    public void addCard(SubDeck subDeck, String blueprintId) {
        _subDecks.computeIfAbsent(subDeck, k -> new LinkedList<>());
        _subDecks.get(subDeck).add(blueprintId);
    }
    public List<String> getDrawDeckCards() { return _subDecks.get(SubDeck.DRAW_DECK); }
    public List<String> getAllCards() {
        List<String> result = new LinkedList<>();
        for (List<String> subDeckCards : _subDecks.values()) {
            result.addAll(subDeckCards);
        }
        return result;
    }

    public Map<SubDeck, List<String>> getSubDecks() { return _subDecks; }
    public List<String> getSubDeck(SubDeck subDeck) { return _subDecks.get(subDeck); }

    public void setSubDecks(Map<SubDeck, List<String>> subDecks) { _subDecks = subDecks; }
    public String getTargetFormat() { return _targetFormat; }
    public String getNotes() {
        return _notes;
    }
}