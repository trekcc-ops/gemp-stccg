package com.gempukku.lotro.cards.sets;

import java.util.List;
import java.util.Set;

public interface SetDefinition {

    String getSetId();

    boolean hasFlag(String flag);

    List<String> getCardsOfRarity(String rarity);

    List<String> getTengwarCards();

    String getCardRarity(String cardId);

    Set<String> getAllCards();
}
