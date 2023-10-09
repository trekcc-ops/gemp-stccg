package com.gempukku.stccg.fieldprocessor;

import com.gempukku.stccg.cards.BuiltLotroCardBlueprint;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.requirement.Requirement;
import org.json.simple.JSONObject;

public class RequirementFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, Object value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        final JSONObject[] requirementsArray = FieldUtils.getObjectArray(value, key);
        for (JSONObject requirement : requirementsArray) {
            final Requirement<DefaultGame> conditionRequirement = environment.getRequirementFactory().getRequirement(requirement, environment);
            blueprint.appendPlayRequirement(conditionRequirement);
        }
    }
}
