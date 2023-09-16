package com.gempukku.lotro.game;

import com.gempukku.lotro.cards.lotronly.LotroDeck;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectionUtils {
    public static Map<String, Integer> getTotalCardCountForDeck(LotroDeck deck) {
        Map<String, Integer> counts = new HashMap<>();
        String ring = deck.getRing();
        if (ring != null)
            incrementCardCount(counts, ring);
        String ringBearer = deck.getRingBearer();
        if (ringBearer != null)
            incrementCardCount(counts, ringBearer);
        for (String site : deck.getSites())
            incrementCardCount(counts, site);
        for (String adventureCard : deck.getDrawDeckCards())
            incrementCardCount(counts, adventureCard);
        return counts;
    }

    public static Map<String, Integer> getTotalCardCount(List<String> cards) {
        Map<String, Integer> counts = new HashMap<>();
        for (String card : cards)
            incrementCardCount(counts, card);
        return counts;
    }

    private static void incrementCardCount(Map<String, Integer> map, String blueprintId) {
        map.merge(blueprintId, 1, Integer::sum);
    }
}
