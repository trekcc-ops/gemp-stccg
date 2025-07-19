package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.game.DefaultGame;

import java.util.ArrayList;
import java.util.List;

public class MultiplyEvaluator extends Evaluator {

    private final List<Evaluator> _evaluators;

    public MultiplyEvaluator(Evaluator... evaluators) {
        super();
        _evaluators = List.of(evaluators);
    }

    public MultiplyEvaluator(float multiplier, Evaluator... evaluators) {
        super();
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