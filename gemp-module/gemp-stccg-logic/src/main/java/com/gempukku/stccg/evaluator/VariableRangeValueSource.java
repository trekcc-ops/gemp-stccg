package com.gempukku.stccg.evaluator;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.game.DefaultGame;

public class VariableRangeValueSource extends ValueSource {

    private final ValueSource _fromValue;
    private final ValueSource _toValue;

    public VariableRangeValueSource(
            @JsonProperty(value = "from", required = true)
            ValueSource fromValue,
            @JsonProperty(value = "to", required = true)
            ValueSource toValue) {
        _fromValue = fromValue;
        _toValue = toValue;
    }

    protected Evaluator getEvaluator(ActionContext actionContext) {
        throw new RuntimeException("Evaluator has resolved to range");
    }

    @Override
    public float getMinimum(DefaultGame cardGame, ActionContext actionContext) {
        return _fromValue.evaluateExpression(cardGame, actionContext);
    }

    @Override
    public float getMaximum(DefaultGame cardGame, ActionContext actionContext) {
        return _toValue.evaluateExpression(cardGame, actionContext);
    }

}