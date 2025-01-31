package com.gempukku.stccg.packs;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.GenericCardItem;
import com.gempukku.stccg.cards.SetDefinition;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class BoosterPack implements PackBox {
    private final String _setId;
    private final Map<String, Integer> _rarityCounts = new HashMap<>();
    private final String _name;

    BoosterPack(
            @JsonProperty(value = "set", required = true)
            String setId,
            @JsonProperty("items")
            List<String> rarityCounts,
            @JsonProperty("name")
            String name) {
        _setId = setId.strip();
        _name = name;
        for (String rarityCount : rarityCounts) {
            String[] result = rarityCount.split("x", 2);
            _rarityCounts.put(result[1], Integer.parseInt(result[0]));
        }
    }

    @Override
    public List<GenericCardItem> openPack(CardBlueprintLibrary library) {
        SetDefinition setDefinition = library.getSetDefinition(_setId);
        List<GenericCardItem> result = new LinkedList<>();
        for (Map.Entry<String, Integer> rarityCount : _rarityCounts.entrySet())
            addRandomCardsOfRarity(setDefinition, result, rarityCount.getValue(), rarityCount.getKey());
        return result;
    }


    private void addRandomCardsOfRarity(SetDefinition setDefinition, Collection<? super GenericCardItem> result,
                                        int count, String rarity) {
        final List<String> cardsOfRarity = setDefinition.getCardsOfRarity(rarity);
        for (Integer cardIndex : getRandomIndices(count, cardsOfRarity.size()))
            result.add(GenericCardItem.createItem(cardsOfRarity.get(cardIndex), 1));
    }

    private Set<Integer> getRandomIndices(int count, int elementCount) {
        Set<Integer> addedIndices = new HashSet<>();
        for (int i = 0; i < count; i++) {
            int index;
            do {
                index = ThreadLocalRandom.current().nextInt(elementCount);
            } while (addedIndices.contains(index));
            addedIndices.add(index);
        }
        return addedIndices;
    }

    public String getName() { return _name; }

}