package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import org.json.simple.JSONObject;

public class OrRequirementProducer extends RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "requires");

        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");

        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);

        return (actionContext) -> RequirementUtils.acceptsAnyRequirements(requirements, actionContext);
    }
}
