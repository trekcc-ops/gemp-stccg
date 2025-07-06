package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.game.DefaultGame;

import java.util.List;

public class AdditionEvaluator extends Evaluator {

    private final List<Evaluator> _evaluators;

    public AdditionEvaluator(Evaluator... evaluators) {
        super();
        _evaluators = List.of(evaluators);
    }

    @Override
    public int evaluateExpression(DefaultGame cardGame) {
        int subtotal = 0;
        for (Evaluator evaluator : _evaluators) {
            subtotal = subtotal + evaluator.evaluateExpression(cardGame);
        }
        return subtotal;
    }
}