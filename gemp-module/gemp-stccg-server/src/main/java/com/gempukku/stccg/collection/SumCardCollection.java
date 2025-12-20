package com.gempukku.stccg.collection;

import com.gempukku.stccg.cards.GenericCardItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SumCardCollection implements CardCollection {
    private final List<? extends CardCollection> _cardCollections;

    public SumCardCollection(List<? extends CardCollection> cardCollections) {
        _cardCollections = cardCollections;
    }

    @Override
    public int getCurrency() {
        int sum = 0;
        for (CardCollection cardCollection : _cardCollections)
            sum += cardCollection.getCurrency();

        return sum;
    }

    @Override
    public Map<String, Object> getExtraInformation() {
        Map<String, Object> result = new HashMap<>();
        for (CardCollection cardCollection : _cardCollections) {
            result.putAll(cardCollection.getExtraInformation());
        }
        return result;
    }

    @Override
    public Iterable<GenericCardItem> getAll() {
        Map<String, GenericCardItem> sum = new HashMap<>();
        for (CardCollection cardCollection : _cardCollections) {
            Iterable<GenericCardItem> inCollection = cardCollection.getAll();
            for (GenericCardItem cardCount : inCollection) {
                String cardId = cardCount.getBlueprintId();
                int count = sum.get(cardId).getCount();
                sum.put(cardId, GenericCardItem.createItem(cardId, count + cardCount.getCount()));
            }
        }

        return sum.values();
    }

    @Override
    public int getItemCount(String blueprintId) {
        int sum = 0;
        for (CardCollection cardCollection : _cardCollections)
            sum += cardCollection.getItemCount(blueprintId);

        return sum;
    }

}