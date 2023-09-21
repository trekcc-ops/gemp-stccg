package com.gempukku.lotro.requirement;

import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.ValueSource;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.effectappender.resolver.ValueResolver;
import org.json.simple.JSONObject;

public class IsNotEqual implements RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "firstNumber", "secondNumber");

        final ValueSource firstNumber = ValueResolver.resolveEvaluator(object.get("firstNumber"), environment);
        final ValueSource secondNumber = ValueResolver.resolveEvaluator(object.get("secondNumber"), environment);

        return actionContext -> {
            final int first = firstNumber.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
            final int second = secondNumber.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
            return first != second;
        };
    }
}
