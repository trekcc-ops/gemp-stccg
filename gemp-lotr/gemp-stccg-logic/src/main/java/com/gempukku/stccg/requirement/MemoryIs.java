package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import org.json.simple.JSONObject;

public class MemoryIs extends RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(object, "memory", "value");

        final String memory = environment.getString(object.get("memory"), "memory");
        final String value = environment.getString(object.get("value"), "value");

        return (actionContext) -> {
            String valueFromMemory = actionContext.getValueFromMemory(memory);
            return valueFromMemory != null && valueFromMemory.equalsIgnoreCase(value);
        };
    }
}
