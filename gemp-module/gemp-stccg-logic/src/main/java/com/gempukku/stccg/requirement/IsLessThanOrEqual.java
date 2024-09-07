package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.ValueSource;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import org.json.simple.JSONObject;

public class IsLessThanOrEqual extends RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(object, "firstNumber", "secondNumber");

        final ValueSource firstNumber = ValueResolver.resolveEvaluator(object.get("firstNumber"), environment);
        final ValueSource secondNumber = ValueResolver.resolveEvaluator(object.get("secondNumber"), environment);

        return actionContext -> {
            final int first = firstNumber.evaluateExpression(actionContext, null);
            final int second = secondNumber.evaluateExpression(actionContext, null);
            return first <= second;
        };
    }
}
