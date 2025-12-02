package com.gempukku.stccg.evaluator;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

public class MaximumValueSource extends ValueSource {

    private final ValueSource _first;
    private final ValueSource _second;

    public MaximumValueSource(
            @JsonProperty(value = "first", required = true)
            ValueSource first,
            @JsonProperty(value = "second", required = true)
            ValueSource second) {
        _first = first;
        _second = second;
    }

    @Override
    public float evaluateExpression(DefaultGame cardGame, ActionContext actionContext) throws InvalidGameLogicException {
        return Math.max(
                _first.evaluateExpression(cardGame, actionContext),
                _second.evaluateExpression(cardGame, actionContext)
        );
    }
}