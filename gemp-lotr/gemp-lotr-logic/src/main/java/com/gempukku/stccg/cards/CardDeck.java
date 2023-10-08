package com.gempukku.stccg.cards;

import com.gempukku.stccg.common.filterable.SubDeck;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CardDeck {
    Map<String, List<String>> _subDecks = new HashMap<>();
    protected final String _deckName;
    protected String _notes;
    protected String _targetFormat;
    protected CardBlueprintLibrary _library;
    public CardDeck(String deckName) {
        _deckName = deckName;
    }
    public CardDeck(CardDeck deck) {
        _deckName = deck.getDeckName();
        _subDecks = deck.getSubDecks();
        _notes = deck.getNotes();
        _targetFormat = deck.getTargetFormat();
        _library = deck.getLibrary();
    }

    public CardDeck(String deckName, String contents, String targetFormat, String notes, CardBlueprintLibrary library) {
        _deckName = deckName;
        _targetFormat = targetFormat;
        _notes = notes;
        parseContents(contents);
        _library = library;
    }

    public void parseContents(String contents) {
        String[] parts = contents.split("\\|");
        for (int i = 0; i < parts.length; i += 2) {
            List<String> cards = new ArrayList<>();
            for (String card : parts[i+1].split(",")) {
                if (!card.equals("")) {
                    cards.add(card);
                }
            }
            _subDecks.put(parts[i], cards);
        }
    }

    public String buildContentsFromDeck() {
        List<String> parts = new ArrayList<>();
        _subDecks.forEach((k,v) -> {
            parts.add(k);
            parts.add(String.join(",",v));
        });
        return String.join("|", parts);
    }

    public String getDeckName() {
        return _deckName;
    }
    public void addCard(String card) {
        _subDecks.get("DRAW_DECK").add(card);
    }
    public List<String> getDrawDeckCards() {
        return _subDecks.get("DRAW_DECK");
    }
    public List<String> getAllDeckCards() {
        List<String> allCards = new ArrayList<>();
        _subDecks.forEach((k, v) -> allCards.addAll(v));
        return allCards;
    }
    public Map<String, List<String>> getSubDecks() { return _subDecks; }
    public Map<SubDeck, List<String>> getSubDecksWithEnum() {
        Map<SubDeck, List<String>> subDecksWithEnum = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : _subDecks.entrySet()) {
            subDecksWithEnum.put(SubDeck.valueOf(entry.getKey()), entry.getValue());
        }
        return subDecksWithEnum;
    }

    public List<String> getSubDeck(SubDeck subDeck) {
        return _subDecks.get(subDeck.name());
    }

    public void setSubDecks(Map<String, List<String>> subDecks) { _subDecks = subDecks; }
    public String getTargetFormat() { return _targetFormat; }
    public CardBlueprintLibrary getLibrary() { return _library; }

    public String getNotes() {
        return _notes;
    }
    public void setNotes(String value) {
        _notes = value;
    }
}
