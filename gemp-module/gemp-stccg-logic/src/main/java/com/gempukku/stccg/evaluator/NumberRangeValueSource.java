package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.game.DefaultGame;

public class NumberRangeValueSource implements ValueSource {

    private final int _min;
    private final int _max;

    public NumberRangeValueSource(int min, int max) {
        _min = min;
        _max = max;
    }

    @Override
    public Evaluator getEvaluator(ActionContext actionContext) {
        throw new RuntimeException("Evaluator has resolved to range");
    }

    @Override
    public float getMinimum(DefaultGame cardGame, ActionContext actionContext) {
        return _min;
    }

    @Override
    public float getMaximum(DefaultGame cardGame, ActionContext actionContext) {
        return _max;
    }

}