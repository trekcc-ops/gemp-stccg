package com.gempukku.stccg.cards;

import java.util.List;
import java.util.Set;

public interface SetDefinition {

    String getSetId();
    String getSetName();

    boolean hasFlag(String flag);

    List<String> getCardsOfRarity(String rarity);

    List<String> getTengwarCards();

    String getCardRarity(String cardId);

    Set<String> getAllCards();
}
