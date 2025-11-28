package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.game.DefaultGame;

public class MaximumValueSource implements ValueSource {

    private final ValueSource _first;
    private final ValueSource _second;

    public MaximumValueSource(ValueSource first, ValueSource second) {
        _first = first;
        _second = second;
    }

    public Evaluator getEvaluator(ActionContext actionContext) {
        return new Evaluator() {
            @Override
            public float evaluateExpression(DefaultGame game) {
                return Math.max(
                        _first.evaluateExpression(game, actionContext),
                        _second.evaluateExpression(game, actionContext)
                );
            }
        };
    }

}