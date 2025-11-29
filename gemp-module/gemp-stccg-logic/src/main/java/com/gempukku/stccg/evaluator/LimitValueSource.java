package com.gempukku.stccg.evaluator;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Objects;

public class LimitValueSource extends ValueSource {
    private final ValueSource _limit;
    private final ValueSource _value;

    public LimitValueSource(
            @JsonProperty(value = "limit")
            ValueSource limitSource,
            @JsonProperty(value = "value")
            ValueSource valueSource
    ) {
        _limit = Objects.requireNonNullElse(limitSource, new ConstantValueSource(1));
        _value = Objects.requireNonNullElse(valueSource, new ConstantValueSource(0));
    }

    protected Evaluator getEvaluator(ActionContext actionContext) {
        return new Evaluator() {

            @Override
            public float evaluateExpression(DefaultGame cardGame) {
                return Math.min( _limit.evaluateExpression(cardGame, actionContext),
                        _value.evaluateExpression(cardGame, actionContext));
            }
        };
    }

}