package com.gempukku.stccg.requirement.producers;

import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.ValueSource;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.requirement.RequirementProducer;
import org.json.simple.JSONObject;

public class IsEqual extends RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "firstNumber", "secondNumber");

        final ValueSource firstNumber = ValueResolver.resolveEvaluator(object.get("firstNumber"), environment);
        final ValueSource secondNumber = ValueResolver.resolveEvaluator(object.get("secondNumber"), environment);

        return actionContext -> {
            final int first = firstNumber.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
            final int second = secondNumber.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
            return first == second;
        };
    }
}
