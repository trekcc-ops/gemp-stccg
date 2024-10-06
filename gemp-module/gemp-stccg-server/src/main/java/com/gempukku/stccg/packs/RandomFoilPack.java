package com.gempukku.stccg.packs;

import com.gempukku.stccg.cards.GenericCardItem;
import com.gempukku.stccg.cards.CardBlueprintLibrary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomFoilPack implements PackBox {
    private final List<String> _availableCards = new ArrayList<>();

    public RandomFoilPack(String[] rarities, String[] sets, CardBlueprintLibrary library) {
        var setList = Arrays.stream(sets).toList();
        for (var setDefinition : library.getSetDefinitions().values()) {
            if(!setList.contains(setDefinition.getSetId()))
                continue;
            for(String rarity : rarities) {
                _availableCards.addAll(setDefinition.getCardsOfRarity(rarity));
            }
        }
    }

    @Override
    public List<GenericCardItem> openPack() {
        return openPack(ThreadLocalRandom.current().nextInt(_availableCards.size()));
    }

    @Override
    public List<GenericCardItem> openPack(int selection) {
        final String bpID = _availableCards.stream().skip(selection).findFirst().orElse(null) + "*";
        return Collections.singletonList(GenericCardItem.createItem(bpID, 1, true));
    }

}
