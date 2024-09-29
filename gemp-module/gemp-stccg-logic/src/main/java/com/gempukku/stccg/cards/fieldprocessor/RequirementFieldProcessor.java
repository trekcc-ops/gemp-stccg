package com.gempukku.stccg.cards.fieldprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.requirement.Requirement;

public class RequirementFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, JsonNode value, CardBlueprint blueprint, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        if (value.isArray()) {
            for (JsonNode requirement : value) {
                final Requirement conditionRequirement = environment.getRequirement(requirement);
                blueprint.appendPlayRequirement(conditionRequirement);
            }
        }
        else throw new InvalidCardDefinitionException("Requirements JSON syntax could not be converted to an array");
    }
}
