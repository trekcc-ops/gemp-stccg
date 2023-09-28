package com.gempukku.lotro.cards;

import java.util.*;

public class CardDeck {
    protected List<String> _drawDeckCards = new ArrayList<>();
    Map<String, List<String>> _subDecks = new HashMap<>();
    protected final String _deckName;
    protected String _notes;
    protected String _targetFormat;
    public CardDeck(String deckName) {
        _deckName = deckName;
    }
    public CardDeck(String deckName, String contents, String targetFormat, String notes) {
        // Assumes "new format" of LotR Gemp syntax
        String[] parts = contents.split("\\|");
        _deckName = deckName;
        _targetFormat = targetFormat;
        _notes = notes;

        for (int i = 0; i < parts.length; i += 2) {
            List<String> cards = new ArrayList<>();
            for (String card : parts[i+1].split(",")) {
                if (!card.equals(""))
                    cards.add(card);
            }
            _subDecks.put(parts[i], cards);
        }
        _drawDeckCards = _subDecks.get("DRAW_DECK");
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
        _drawDeckCards.add(card);
    }
    public List<String> getDrawDeckCards() {
        return Collections.unmodifiableList(_drawDeckCards);
    }
    public String getTargetFormat() { return _targetFormat; }
    public void setTargetFormat(String value) { _targetFormat = value; }
    public String getNotes() {
        return _notes;
    }
    public void setNotes(String value) {
        _notes = value;
    }
}
