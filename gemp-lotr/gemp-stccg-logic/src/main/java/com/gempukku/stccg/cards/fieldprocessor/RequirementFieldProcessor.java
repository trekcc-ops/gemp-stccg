package com.gempukku.stccg.cards.fieldprocessor;

import com.gempukku.stccg.cards.CardBlueprint;
import com.gempukku.stccg.cards.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.requirement.Requirement;
import org.json.simple.JSONObject;

public class RequirementFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, Object value, CardBlueprint blueprint, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        final JSONObject[] requirementsArray = environment.getObjectArray(value, key);
        for (JSONObject requirement : requirementsArray) {
            final Requirement conditionRequirement = environment.getRequirement(requirement);
            blueprint.appendPlayRequirement(conditionRequirement);
        }
    }
}
