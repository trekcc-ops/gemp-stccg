package com.gempukku.stccg.requirement;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.ValueSource;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;

public class IsNotEqual extends RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JsonNode node, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(node, "firstNumber", "secondNumber");

        final ValueSource firstNumber = ValueResolver.resolveEvaluator(node.get("firstNumber"), environment);
        final ValueSource secondNumber = ValueResolver.resolveEvaluator(node.get("secondNumber"), environment);

        return actionContext -> {
            final int first = firstNumber.evaluateExpression(actionContext, null);
            final int second = secondNumber.evaluateExpression(actionContext, null);
            return first != second;
        };
    }
}
