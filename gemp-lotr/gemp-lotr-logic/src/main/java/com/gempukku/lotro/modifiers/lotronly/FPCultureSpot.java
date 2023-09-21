package com.gempukku.lotro.modifiers.lotronly;

import com.gempukku.lotro.cards.*;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.effectappender.resolver.ValueResolver;
import com.gempukku.lotro.modifiers.ModifierSourceProducer;
import com.gempukku.lotro.modifiers.RequirementCondition;
import com.gempukku.lotro.requirement.Requirement;
import org.json.simple.JSONObject;

public class FPCultureSpot implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "requires", "amount");

        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");
        final ValueSource amount = ValueResolver.resolveEvaluator(object.get("amount"), environment);

        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);

        return actionContext -> new FPCulturesSpotCountModifier(actionContext.getSource(),
                new RequirementCondition(requirements, actionContext),
                amount.getEvaluator(actionContext));
    }
}
