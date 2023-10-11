package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class ConstantEvaluator implements Evaluator {
    private final int _value;

    public ConstantEvaluator(int value) {
        _value = value;
    }

    @Override
    public int evaluateExpression(DefaultGame game, PhysicalCard self) {
        return _value;
    }
}
