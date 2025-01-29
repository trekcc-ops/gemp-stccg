package com.gempukku.stccg.requirement;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.evaluator.ValueSource;
import com.gempukku.stccg.evaluator.ValueResolver;

public class ComparatorRequirement implements Requirement {

    private enum ComparatorType {
        ISEQUAL, ISGREATERTHAN, ISGREATERTHANOREQUAL,
        ISLESSTHAN, ISLESSTHANOREQUAL, ISNOTEQUAL
    }

    private final ComparatorType _comparatorType;
    private final ValueSource _firstNumber;
    private final ValueSource _secondNumber;

    public ComparatorRequirement(JsonNode node) throws InvalidCardDefinitionException {
        _comparatorType = BlueprintUtils.getEnum(ComparatorType.class, node, "type");
        if (_comparatorType == null)
            throw new InvalidCardDefinitionException("Comparator requirement type not found");

        BlueprintUtils.validateAllowedFields(node, "firstNumber", "secondNumber");

        _firstNumber = ValueResolver.resolveEvaluator(node.get("firstNumber"));
        _secondNumber = ValueResolver.resolveEvaluator(node.get("secondNumber"));
    }

    public boolean accepts(ActionContext actionContext) {
        final int firstQuantity = _firstNumber.evaluateExpression(actionContext, null);
        final int secondQuantity = _secondNumber.evaluateExpression(actionContext, null);
        return switch(_comparatorType) {
                case ISEQUAL -> firstQuantity == secondQuantity;
                case ISGREATERTHAN -> firstQuantity > secondQuantity;
                case ISGREATERTHANOREQUAL -> firstQuantity >= secondQuantity;
                case ISLESSTHAN -> firstQuantity < secondQuantity;
                case ISLESSTHANOREQUAL -> firstQuantity <= secondQuantity;
                case ISNOTEQUAL -> firstQuantity != secondQuantity;
            };
    }
}