package com.gempukku.stccg.player;

import com.gempukku.stccg.cards.InvalidCardDefinitionException;

import java.util.List;

public class PlayerResolver {

    public static PlayerSource resolvePlayer(String type) throws InvalidCardDefinitionException {

        if (type.equalsIgnoreCase("you")) {
            return new YouPlayerSource();
        } else if (List.of("opponent", "youropponent").contains(type.toLowerCase())) {
            return new YourOpponentPlayerSource();
        } else if (type.toLowerCase().startsWith("frommemory(") && type.endsWith(")")) {
            String memory = type.substring(type.indexOf("(") + 1, type.lastIndexOf(")"));
            return new PlayerSourceFromMemory(memory);
        } else {
            throw new InvalidCardDefinitionException("Unable to resolve player resolver of type: " + type);
        }
    }
}