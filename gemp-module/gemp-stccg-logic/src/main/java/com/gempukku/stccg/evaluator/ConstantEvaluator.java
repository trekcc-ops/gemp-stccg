package com.gempukku.stccg.evaluator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.game.DefaultGame;

public class ConstantEvaluator extends Evaluator {

    @JsonProperty("value")
    private final float _value;

    @JsonCreator
    public ConstantEvaluator(@JsonProperty(value = "value", required = true) float value) {
        super();
        _value = value;
    }

    @Override
    public float evaluateExpression(DefaultGame cardGame) {
        return _value;
    }
}