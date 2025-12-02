package com.gempukku.stccg.player;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;

public class PlayerResolver {

    public static PlayerSource resolvePlayer(String type) throws InvalidCardDefinitionException {

        if (type.equalsIgnoreCase("you"))
            return ActionContext::getPerformingPlayerId;
        else {
            String memory = type.substring(type.indexOf("(") + 1, type.lastIndexOf(")"));
            if (type.toLowerCase().startsWith("frommemory(") && type.endsWith(")")) {
                return (actionContext) -> actionContext.getValueFromMemory(memory);
            }
        }
        throw new InvalidCardDefinitionException("Unable to resolve player resolver of type: " + type);
    }
}