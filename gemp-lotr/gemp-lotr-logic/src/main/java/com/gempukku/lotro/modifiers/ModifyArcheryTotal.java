package com.gempukku.lotro.modifiers;

import com.gempukku.lotro.cards.*;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.effectappender.resolver.ValueResolver;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.modifiers.lotronly.ArcheryTotalModifier;
import com.gempukku.lotro.evaluator.Evaluator;
import com.gempukku.lotro.requirement.Requirement;
import org.json.simple.JSONObject;

public class ModifyArcheryTotal implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "side", "requires", "amount");

        final ValueSource valueSource = ValueResolver.resolveEvaluator(object.get("amount"), environment);
        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");
        final Side side = FieldUtils.getSide(object.get("side"), "side");

        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);

        return (actionContext) -> {
            final Evaluator evaluator = valueSource.getEvaluator(actionContext);
            return new ArcheryTotalModifier(actionContext.getSource(), side,
                    new RequirementCondition(requirements, actionContext), evaluator);
        };
    }
}
