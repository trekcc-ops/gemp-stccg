package com.gempukku.stccg.evaluator;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

public class MaximumValueSource implements SingleValueSource {

    private final SingleValueSource _first;
    private final SingleValueSource _second;

    public MaximumValueSource(
            @JsonProperty(value = "first", required = true)
            SingleValueSource first,
            @JsonProperty(value = "second", required = true)
            SingleValueSource second) {
        _first = first;
        _second = second;
    }

    @Override
    public int evaluateExpression(DefaultGame cardGame, ActionContext actionContext) throws InvalidGameLogicException {
        return Math.max(
                _first.evaluateExpression(cardGame, actionContext),
                _second.evaluateExpression(cardGame, actionContext)
        );
    }
}