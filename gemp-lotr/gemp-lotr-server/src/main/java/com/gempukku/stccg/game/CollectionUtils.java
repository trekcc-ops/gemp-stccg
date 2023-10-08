package com.gempukku.stccg.game;

import com.gempukku.stccg.cards.CardDeck;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectionUtils {
    public static Map<String, Integer> getTotalCardCountForDeck(CardDeck deck) {
        Map<String, Integer> counts = new HashMap<>();
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
