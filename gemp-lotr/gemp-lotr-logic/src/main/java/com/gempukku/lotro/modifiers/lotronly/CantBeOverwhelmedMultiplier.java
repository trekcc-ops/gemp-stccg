package com.gempukku.lotro.modifiers.lotronly;

import com.gempukku.lotro.cards.*;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.modifiers.ModifierSourceProducer;
import com.gempukku.lotro.modifiers.RequirementCondition;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.requirement.Requirement;
import org.json.simple.JSONObject;

public class CantBeOverwhelmedMultiplier implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "filter", "requires", "multiplier");

        final String filter = FieldUtils.getString(object.get("filter"), "filter");
        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");
        final int multiplier = FieldUtils.getInteger(object.get("multiplier"), "multiplier", 3);

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);

        return actionContext -> {
            final Filterable filterable = filterableSource.getFilterable(actionContext);
            return new OverwhelmedByMultiplierModifier(actionContext.getSource(), filterable,
                    new RequirementCondition(requirements, actionContext),
                    multiplier);
        };
    }
}
