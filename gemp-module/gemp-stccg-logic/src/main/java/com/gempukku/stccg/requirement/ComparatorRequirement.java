package com.gempukku.stccg.requirement;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.evaluator.ValueSource;

public class ComparatorRequirement implements Requirement {

    public enum ComparatorType {
        ISEQUAL, ISGREATERTHAN, ISGREATERTHANOREQUAL,
        ISLESSTHAN, ISLESSTHANOREQUAL, ISNOTEQUAL
    }

    private final ComparatorType _comparatorType;
    private final ValueSource _firstNumber;
    private final ValueSource _secondNumber;

    public ComparatorRequirement(@JsonProperty("firstNumber")
                                 ValueSource firstNumber,
                                 @JsonProperty("secondNumber")
                                 ValueSource secondNumber,
                                 @JsonProperty("type")
                                 ComparatorType type) {
        _comparatorType = type;
        _firstNumber = firstNumber;
        _secondNumber = secondNumber;
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