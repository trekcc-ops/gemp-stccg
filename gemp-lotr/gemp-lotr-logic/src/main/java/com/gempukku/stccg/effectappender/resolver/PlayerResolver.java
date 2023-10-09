package com.gempukku.stccg.effectappender.resolver;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.cards.PlayerSource;

import java.util.Locale;

public class PlayerResolver {
    public static PlayerSource resolvePlayer(String type) throws InvalidCardDefinitionException {

        if (type.equalsIgnoreCase("you"))
            return ActionContext::getPerformingPlayer;
        if (type.equalsIgnoreCase("owner"))
            return (actionContext) -> actionContext.getSource().getOwner();
        else {
            String memory = type.substring(type.indexOf("(") + 1, type.lastIndexOf(")"));
            if (type.toLowerCase(Locale.ROOT).startsWith("ownerfrommemory(") && type.endsWith(")")) {
                return (actionContext) -> {
                    final PhysicalCard cardFromMemory = actionContext.getCardFromMemory(memory);
                    if (cardFromMemory != null)
                        return cardFromMemory.getOwner();
                    else
                        // Sensible default
                        return actionContext.getPerformingPlayer();
                };
            }
            else if (type.toLowerCase().startsWith("frommemory(") && type.endsWith(")")) {
                return (actionContext) -> actionContext.getValueFromMemory(memory);
            }
        }
        throw new InvalidCardDefinitionException("Unable to resolve player resolver of type: " + type);
    }
}
