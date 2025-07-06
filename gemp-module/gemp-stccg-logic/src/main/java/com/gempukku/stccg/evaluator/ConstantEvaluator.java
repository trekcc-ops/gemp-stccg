package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class ConstantEvaluator extends Evaluator {
    private final float _value;

    public ConstantEvaluator(int value) {
        super();
        _value = value;
    }

    @Override
    public float evaluateExpression(DefaultGame cardGame) {
        return _value;
    }
}