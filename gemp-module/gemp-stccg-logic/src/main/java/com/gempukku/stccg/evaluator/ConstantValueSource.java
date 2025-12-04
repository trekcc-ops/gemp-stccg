package com.gempukku.stccg.evaluator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.game.DefaultGame;

public class ConstantValueSource extends BasicValueSource {
    private final int _value;

    @JsonCreator
    public ConstantValueSource(int value) { _value = value; }

    @Override
    public float evaluateExpression(DefaultGame cardGame, ActionContext actionContext) {
        return _value;
    }
}