package com.gempukku.lotro.game.modifiers.evaluator;

import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.game.DefaultGame;

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