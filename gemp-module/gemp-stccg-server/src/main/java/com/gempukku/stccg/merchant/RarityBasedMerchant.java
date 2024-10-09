package com.gempukku.stccg.merchant;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.SetDefinition;

import java.util.HashMap;
import java.util.Map;

public class RarityBasedMerchant {
    private final Map<String, SetDefinition> _rarity = new HashMap<>();

    public RarityBasedMerchant(CardBlueprintLibrary library) {
        for (SetDefinition setDefinition : library.getSetDefinitions().values()) {
            if (setDefinition.hasFlag("merchantable"))
                _rarity.put(setDefinition.getSetId(), setDefinition);
        }
    }

    public Integer getCardSellPrice(String blueprintId) {
        boolean foil = false;
        if (blueprintId.endsWith("*")) {
            foil = true;
            blueprintId = blueprintId.substring(0, blueprintId.length() - 1);
        }

        if (foil)
            return null;

        return getCardBasePrice(blueprintId);
    }

    public Integer getCardBuyPrice(String blueprintId) {
        boolean foil = false;
        if (blueprintId.endsWith("*")) {
            foil = true;
            blueprintId = blueprintId.substring(0, blueprintId.length() - 1);
        }
        Integer cardBasePrice = getCardBasePrice(blueprintId);
        if (cardBasePrice == null)
            return null;

        if (foil)
            cardBasePrice *= 4;

        return cardBasePrice / 4;
    }

    private Integer getCardBasePrice(String blueprintId) {
        final int underscoreIndex = blueprintId.indexOf("_");
        if (underscoreIndex<0)
            return null;
        SetDefinition rarity = _rarity.get(blueprintId.substring(0, underscoreIndex));
        if (rarity == null)
            return null;
        String cardRarity = rarity.getCardRarity(blueprintId);
        return switch (cardRarity) {
            case "C" -> 50;
            case "U", "S" -> 100;
            case "R", "P", "A" -> 1000;
            case "X" -> 2000;
            default -> throw new RuntimeException("Unknown rarity for priced card: " + cardRarity);
        };
    }

}
