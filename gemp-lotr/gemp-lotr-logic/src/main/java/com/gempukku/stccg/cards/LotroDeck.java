package com.gempukku.stccg.cards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LotroDeck extends CardDeck {
    private String _ringBearer;
    private String _ring;
    private final List<String> _siteCards = new ArrayList<>();

    public LotroDeck(String deckName) {
        super(deckName);
        _targetFormat = "Anything Goes";
    }

    public void setRingBearer(String ringBearer) {
        _ringBearer = ringBearer;
    }

    public void setRing(String ring) {
        _ring = ring;
    }

    public void addSite(String card) {
        _siteCards.add(card);
    }

    public void emptyDrawDeck() {
        _subDecks.get("DRAW_DECK").clear();
    }

    public List<String> getSites() {
        return Collections.unmodifiableList(_siteCards);
    }

    public String getRing() {
        return _ring;
    }

    public String buildContentsFromDeck() {
        StringBuilder sb = new StringBuilder();
        if (_ringBearer != null)
            sb.append(_ringBearer);
        sb.append("|");
        if (_ring != null)
            sb.append(_ring);
        sb.append("|");
        for (int i = 0; i < this.getSites().size(); i++) {
            if (i > 0)
                sb.append(",");
            sb.append(this.getSites().get(i));
        }
        sb.append("|");
        for (int i = 0; i < this.getDrawDeckCards().size(); i++) {
            if (i > 0)
                sb.append(",");
            sb.append(this.getDrawDeckCards().get(i));
        }

        return sb.toString();
    }
}
