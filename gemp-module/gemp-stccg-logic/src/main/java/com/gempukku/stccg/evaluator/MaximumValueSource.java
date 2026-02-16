package com.gempukku.stccg.evaluator;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;

public class MaximumValueSource implements SingleValueSource {

    private final static int MIN_VALUE = -99999;
    private final Collection<SingleValueSource> _values;

    public MaximumValueSource(
            @JsonProperty(value = "values", required = true)
            Collection<SingleValueSource> values) {
        _values = values;
    }

    @Override
    public int evaluateExpression(DefaultGame cardGame, ActionContext actionContext) {
        int maxValue = MIN_VALUE;
        for (SingleValueSource value : _values) {
            maxValue = Math.max(maxValue, value.evaluateExpression(cardGame, actionContext));
        }
        return maxValue;
    }
}