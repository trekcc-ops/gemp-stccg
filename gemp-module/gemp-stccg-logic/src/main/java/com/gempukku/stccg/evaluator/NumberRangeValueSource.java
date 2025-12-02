package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

public class NumberRangeValueSource extends ValueSource {

    private final int _min;
    private final int _max;

    public NumberRangeValueSource(int min, int max) {
        _min = min;
        _max = max;
    }

    @Override
    public float getMinimum(DefaultGame cardGame, ActionContext actionContext) {
        return _min;
    }

    @Override
    public float getMaximum(DefaultGame cardGame, ActionContext actionContext) {
        return _max;
    }

    @Override
    public float evaluateExpression(DefaultGame cardGame, ActionContext actionContext) throws InvalidGameLogicException {
        throw new InvalidGameLogicException("Tried to evaluate a range ValueSource object as a single number");
    }

}