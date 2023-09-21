package com.gempukku.lotro.modifiers;

import com.gempukku.lotro.cards.*;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.effectappender.resolver.ValueResolver;
import com.gempukku.lotro.modifiers.lotronly.SanctuaryHealModifier;
import com.gempukku.lotro.requirement.Requirement;
import org.json.simple.JSONObject;

public class ModifySanctuaryHeal implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "requires", "amount");

        final ValueSource amountSource = ValueResolver.resolveEvaluator(object.get("amount"), environment);
        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");

        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);

        return (actionContext) -> new SanctuaryHealModifier(actionContext.getSource(),
                new RequirementCondition(requirements, actionContext),
                amountSource.getEvaluator(actionContext));
    }
}
