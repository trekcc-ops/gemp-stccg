package com.gempukku.stccg.evaluator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.game.DefaultGame;

import java.util.ArrayList;
import java.util.List;

public class MultiplyEvaluator extends Evaluator {

    @JsonProperty("evaluators")
    private final List<Evaluator> _evaluators;

    @JsonCreator()
    private MultiplyEvaluator(@JsonProperty(value = "evaluators", required = true) List<Evaluator> evaluators) {
        _evaluators = evaluators;
    }

    public MultiplyEvaluator(float multiplier, Evaluator... evaluators) {
        _evaluators = new ArrayList<>();
        _evaluators.addAll(List.of(evaluators));
        _evaluators.add(new ConstantEvaluator(multiplier));
    }

    @Override
    public float evaluateExpression(DefaultGame game) {
        float subtotal = 1;
        for (Evaluator evaluator : _evaluators) {
            subtotal = subtotal * evaluator.evaluateExpression(game);
        }
        return subtotal;
    }
}