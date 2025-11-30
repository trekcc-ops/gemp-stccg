package com.gempukku.stccg.evaluator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.game.DefaultGame;

public class ConstantValueSource extends ValueSource {
    private final int _value;

    @JsonCreator
    public ConstantValueSource(int value) { _value = value; }
    public Evaluator getEvaluator(ActionContext actionContext) {
        return new ConstantEvaluator(_value);
    }

    public float getMinimum(DefaultGame cardGame, ActionContext actionContext) { return _value; }

    public float getMaximum(DefaultGame cardGame, ActionContext actionContext) { return _value; }
}