package com.gempukku.stccg.requirement.producers;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.ValueSource;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.requirement.RequirementProducer;

public class IsLessThan extends RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JsonNode node, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(node, "firstNumber", "secondNumber");

        final ValueSource firstNumber = ValueResolver.resolveEvaluator(node.get("firstNumber"), environment);
        final ValueSource secondNumber = ValueResolver.resolveEvaluator(node.get("secondNumber"), environment);

        return actionContext -> firstNumber.evaluateExpression(actionContext, null) <
                secondNumber.evaluateExpression(actionContext, null);
    }
}
