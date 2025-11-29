package com.gempukku.stccg.evaluator;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.game.DefaultGame;

public class MinimumValueSource extends ValueSource {

    private final ValueSource _first;
    private final ValueSource _second;

    public MinimumValueSource(
            @JsonProperty(value = "first", required = true)
            ValueSource first,
            @JsonProperty(value = "second", required = true)
            ValueSource second) {
        _first = first;
        _second = second;
    }

    protected Evaluator getEvaluator(ActionContext actionContext) {
        return new Evaluator() {
            @Override
            public float evaluateExpression(DefaultGame game) {
                return Math.min(
                        _first.evaluateExpression(game, actionContext),
                        _second.evaluateExpression(game, actionContext)
                );
            }
        };
    }

}