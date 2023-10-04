package com.gempukku.lotro.evaluator;

import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.game.DefaultGame;

public class ConstantEvaluator<AbstractGame extends DefaultGame> implements Evaluator<AbstractGame> {
    private final int _value;

    public ConstantEvaluator(int value) {
        _value = value;
    }

    @Override
    public int evaluateExpression(AbstractGame game, PhysicalCard self) {
        return _value;
    }
}
