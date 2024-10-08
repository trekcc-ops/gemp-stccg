package com.gempukku.stccg.cards.blueprints.requirement;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;

public class ComparatorRequirementProducer extends RequirementProducer {

    private enum ComparatorType {
        ISEQUAL, ISGREATERTHAN, ISGREATERTHANOREQUAL,
        ISLESSTHAN, ISLESSTHANOREQUAL, ISNOTEQUAL
    }
    @Override
    public Requirement getPlayRequirement(JsonNode node)
            throws InvalidCardDefinitionException {
        ComparatorType comparatorType = BlueprintUtils.getEnum(ComparatorType.class, node, "type");
        if (comparatorType == null)
            throw new InvalidCardDefinitionException("Comparator requirement type not found");

        BlueprintUtils.validateAllowedFields(node, "firstNumber", "secondNumber");

        final ValueSource firstNumber = ValueResolver.resolveEvaluator(node.get("firstNumber"));
        final ValueSource secondNumber = ValueResolver.resolveEvaluator(node.get("secondNumber"));

        return actionContext -> {
            final int firstQuantity = firstNumber.evaluateExpression(actionContext, null);
            final int secondQuantity = secondNumber.evaluateExpression(actionContext, null);
            return switch(comparatorType) {
                case ISEQUAL -> firstQuantity == secondQuantity;
                case ISGREATERTHAN -> firstQuantity > secondQuantity;
                case ISGREATERTHANOREQUAL -> firstQuantity >= secondQuantity;
                case ISLESSTHAN -> firstQuantity < secondQuantity;
                case ISLESSTHANOREQUAL -> firstQuantity <= secondQuantity;
                case ISNOTEQUAL -> firstQuantity != secondQuantity;
            };
        };
    }
}