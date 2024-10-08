package com.gempukku.stccg.cards.blueprints.requirement;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;

public class IsLessThan extends RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JsonNode node) throws InvalidCardDefinitionException {
        BlueprintUtils.validateAllowedFields(node, "firstNumber", "secondNumber");

        final ValueSource firstNumber = ValueResolver.resolveEvaluator(node.get("firstNumber"));
        final ValueSource secondNumber = ValueResolver.resolveEvaluator(node.get("secondNumber"));

        return actionContext -> firstNumber.evaluateExpression(actionContext, null) <
                secondNumber.evaluateExpression(actionContext, null);
    }
}