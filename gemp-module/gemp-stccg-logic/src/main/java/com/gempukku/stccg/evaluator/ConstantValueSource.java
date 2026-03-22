package com.gempukku.stccg.evaluator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.game.DefaultGame;

public class ConstantValueSource implements BasicValueSource, BasicSingleValueSource {
    private final int _value;

    @JsonCreator
    public ConstantValueSource(int value) { _value = value; }

    @Override
    public Evaluator getEvaluator(DefaultGame cardGame, GameTextContext context) {
        return new ConstantEvaluator(_value);
    }
}