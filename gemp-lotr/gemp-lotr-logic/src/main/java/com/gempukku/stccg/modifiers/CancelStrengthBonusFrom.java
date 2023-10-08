package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.requirement.Requirement;
import org.json.simple.JSONObject;

public class CancelStrengthBonusFrom implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "from", "requires");

        final String filter = FieldUtils.getString(object.get("from"), "from");
        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);

        return actionContext -> new CancelStrengthBonusSourceModifier(actionContext.getSource(),
                new RequirementCondition(requirements, actionContext),
                filterableSource.getFilterable(actionContext));
    }
}
