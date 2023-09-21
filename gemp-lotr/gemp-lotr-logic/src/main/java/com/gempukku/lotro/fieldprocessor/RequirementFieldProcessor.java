package com.gempukku.lotro.fieldprocessor;

import com.gempukku.lotro.cards.BuiltLotroCardBlueprint;
import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.requirement.Requirement;
import org.json.simple.JSONObject;

public class RequirementFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, Object value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        final JSONObject[] requirementsArray = FieldUtils.getObjectArray(value, key);
        for (JSONObject requirement : requirementsArray) {
            final Requirement conditionRequirement = environment.getRequirementFactory().getRequirement(requirement, environment);
            blueprint.appendPlayRequirement(conditionRequirement);
        }
    }
}
