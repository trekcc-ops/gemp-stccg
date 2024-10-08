package com.gempukku.stccg.cards.blueprints.requirement;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;

public class MemoryLike extends RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JsonNode node) throws InvalidCardDefinitionException {
        BlueprintUtils.validateAllowedFields(node, "memory", "value");

        final String memory = BlueprintUtils.getString(node, "memory");
        final String value = BlueprintUtils.getString(node, "value");

        return (actionContext) -> {
            String valueFromMemory = actionContext.getValueFromMemory(memory);
            return valueFromMemory != null && valueFromMemory.toLowerCase().contains(value.toLowerCase());
        };
    }
}