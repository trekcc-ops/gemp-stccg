package com.gempukku.stccg.packs;

import com.gempukku.stccg.cards.GenericCardItem;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.SetDefinition;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ReflectionsPackBox implements PackBox {
    //private final SetDefinition _reflectionsRarity;
    private final List<String> _previousSetCards = new ArrayList<>();
    private final List<String> _reflectionSlotCards = new ArrayList<>();

    public ReflectionsPackBox(CardBlueprintLibrary library) {
        var reflectionsRarity = library.getSetDefinitions().get("9");

        for (int set = 1; set <= 6; set++) {
            final SetDefinition setRarity = library.getSetDefinitions().get(String.valueOf(set));
            _previousSetCards.addAll(setRarity.getCardsOfRarity("R"));
            _previousSetCards.addAll(setRarity.getCardsOfRarity("P"));
            for (int i = 0; i < 3; i++)
                _previousSetCards.addAll(setRarity.getCardsOfRarity("U"));
            for (int i = 0; i < 7; i++)
                _previousSetCards.addAll(setRarity.getCardsOfRarity("C"));
        }

        _reflectionSlotCards.addAll(reflectionsRarity.getCardsOfRarity("R"));
        _reflectionSlotCards.addAll(reflectionsRarity.getCardsOfRarity("R"));
        _reflectionSlotCards.addAll(reflectionsRarity.getCardsOfRarity("R"));
        _reflectionSlotCards.addAll(reflectionsRarity.getCardsOfRarity("R"));
        _reflectionSlotCards.addAll(reflectionsRarity.getCardsOfRarity("X"));
    }

    @Override
    public List<GenericCardItem> openPack() {
        List<GenericCardItem> result = new LinkedList<>();
        boolean foil;
        foil = ThreadLocalRandom.current().nextInt(15) == 0;
        result.add(GenericCardItem.createItem(getRandomReflectionsCard() + (foil ? "*" : ""), 1));
        foil = ThreadLocalRandom.current().nextInt(15) == 0;
        result.add(GenericCardItem.createItem(getRandomReflectionsCard() + (foil ? "*" : ""), 1));

        for (int i = 0; i < 16; i++) {
            final String blueprintId = _previousSetCards.get(ThreadLocalRandom.current().nextInt(_previousSetCards.size()));
            // There is a 1/6 * 1/11 chance it will be a foil
            if (ThreadLocalRandom.current().nextInt(66) == 0)
                result.add(GenericCardItem.createItem(blueprintId + "*", 1));
            else
                result.add(GenericCardItem.createItem(blueprintId, 1));
        }

        return result;
    }

    public String getRandomReflectionsCard() {
        return _reflectionSlotCards.get(ThreadLocalRandom.current().nextInt(_reflectionSlotCards.size()));
    }

    @Override
    public List<GenericCardItem> openPack(int selection) { return openPack(); }

}