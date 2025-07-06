package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.game.DefaultGame;

public class SingleMemoryEvaluator extends Evaluator {
    private Float _rememberedValue;
    private final Evaluator _evaluator;

    public SingleMemoryEvaluator(Evaluator evaluator) {
        super();
        _evaluator = evaluator;
    }

    @Override
    public float evaluateExpression(DefaultGame game) {
        if (_rememberedValue == null)
            _rememberedValue = _evaluator.evaluateExpression(game);
        return _rememberedValue;
    }
}