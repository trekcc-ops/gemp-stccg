package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import org.json.simple.JSONObject;

public class MemoryLike extends RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "memory", "value");

        final String memory = FieldUtils.getString(object.get("memory"), "memory");
        final String value = FieldUtils.getString(object.get("value"), "value");

        return (actionContext) -> {
            String valueFromMemory = actionContext.getValueFromMemory(memory);
            return valueFromMemory != null && valueFromMemory.toLowerCase().contains(value.toLowerCase());
        };
    }
}
