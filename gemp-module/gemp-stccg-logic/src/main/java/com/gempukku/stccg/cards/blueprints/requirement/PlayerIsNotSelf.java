package com.gempukku.stccg.cards.blueprints.requirement;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.resolver.PlayerResolver;

public class PlayerIsNotSelf extends RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JsonNode node)
            throws InvalidCardDefinitionException {
        BlueprintUtils.validateAllowedFields(node, "memory");

        final String memory = node.get("memory").textValue();
        final PlayerSource selfPlayerSource = PlayerResolver.resolvePlayer("you");

        return (actionContext) -> {
            String selfPlayerId = selfPlayerSource.getPlayerId(actionContext);
            String valueFromMemory = actionContext.getValueFromMemory(memory);
            return valueFromMemory != null && !valueFromMemory.equals(selfPlayerId);
        };
    }
}