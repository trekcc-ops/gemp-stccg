package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

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
