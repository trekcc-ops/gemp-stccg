package com.gempukku.stccg.packs;

import com.gempukku.stccg.cards.GenericCardItem;
import com.gempukku.stccg.cards.SetDefinition;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class BoosterPack implements PackBox {
    private final SetDefinition _setDefinition;
    final Map<String, Integer> _rarityCounts = new HashMap<>();

    public BoosterPack(SetDefinition setDefinition, List<String> rarityCounts) {

        for (String rarityCount : rarityCounts) {
            String[] result = rarityCount.split("x", 2);
            _rarityCounts.put(result[1], Integer.parseInt(result[0]));
        }
        _setDefinition = setDefinition;
    }

    @Override
    public List<GenericCardItem> openPack() {
        List<GenericCardItem> result = new LinkedList<>();
        for (Map.Entry<String, Integer> rarityCount : _rarityCounts.entrySet())
            addRandomCardsOfRarity(result, rarityCount.getValue(), rarityCount.getKey());
        return result;
    }

    private void addRandomCardsOfRarity(List<GenericCardItem> result, int count, String rarity) {
        final List<String> cardsOfRarity = _setDefinition.getCardsOfRarity(rarity);
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

    @Override
    public List<GenericCardItem> openPack(int selection) { return openPack(); }

}
