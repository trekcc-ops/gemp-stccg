package com.gempukku.stccg.evaluator;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

public class VariableRangeValueSource implements ValueSource {

    private final SingleValueSource _fromValue;
    private final SingleValueSource _toValue;

    public VariableRangeValueSource(
            @JsonProperty(value = "from", required = true)
            SingleValueSource fromValue,
            @JsonProperty(value = "to", required = true)
            SingleValueSource toValue) {
        _fromValue = fromValue;
        _toValue = toValue;
    }

    @Override
    public int getMinimum(DefaultGame cardGame, ActionContext actionContext) throws InvalidGameLogicException {
        return _fromValue.evaluateExpression(cardGame, actionContext);
    }

    @Override
    public int getMaximum(DefaultGame cardGame, ActionContext actionContext) throws InvalidGameLogicException {
        return _toValue.evaluateExpression(cardGame, actionContext);
    }

}