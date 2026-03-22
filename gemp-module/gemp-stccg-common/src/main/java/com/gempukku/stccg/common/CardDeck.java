package com.gempukku.stccg.common;

import com.gempukku.stccg.common.filterable.SubDeck;

import java.util.*;

public class CardDeck {

    Map<SubDeck, List<String>> _subDecks = new HashMap<>();

    protected final String _deckName;
    protected final String _notes;
    protected final String _targetFormat;

    public CardDeck(Map<SubDeck, List<String>> subDecks) {
        _subDecks = subDecks;
        _deckName = "";
        _notes = "";
        _targetFormat = "";
    }

    public CardDeck(String deckName, AbstractGameFormat format) {
        _deckName = deckName;
        _targetFormat = format.getName();
        _notes = "";
    }


    public CardDeck(CardDeck deck) {
        this(deck, deck.getDeckName());
    }

    public CardDeck(CardDeck deck, String newName) {
        _deckName = newName;
        _subDecks = deck.getSubDecks();
        _notes = Objects.requireNonNullElse(deck.getNotes(), "");
        _targetFormat = deck.getTargetFormat();
    }

    public CardDeck(String deckName, String contents, String formatName, String notes) {
        _deckName = deckName;
        _targetFormat = formatName;
        _notes = Objects.requireNonNullElse(notes, "");
        parseContents(contents);
    }



    public CardDeck(String deckName, String contents, AbstractGameFormat format, String notes) {
        this(deckName, contents, format.getName(), notes);
    }

    public CardDeck(String deckName, String contents, String formatName) {
        this(deckName, contents, formatName, "");
    }

    public CardDeck(String deckName, String contents, AbstractGameFormat format) {
        this(deckName, contents, format, "");
    }



    public void parseContents(String contents) {
        String[] parts = contents.split("\\|");
        for (int i = 0; i < parts.length; i += 2) {
            List<String> cards = new ArrayList<>();
            // if (i < parts.length - 1) condition needed for decks with empty subdecks
            if (i < parts.length - 1) {
                for (String card : parts[i + 1].split(",")) {
                    if (!card.isEmpty()) {
                        cards.add(card);
                    }
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