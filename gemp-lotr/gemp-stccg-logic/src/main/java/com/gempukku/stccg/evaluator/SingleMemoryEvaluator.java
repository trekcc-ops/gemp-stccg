package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class SingleMemoryEvaluator implements Evaluator {
    private Integer _rememberedValue;
    private final Evaluator _evaluator;

    public SingleMemoryEvaluator(Evaluator evaluator) {
        _evaluator = evaluator;
    }

    @Override
    public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
        if (_rememberedValue == null)
            _rememberedValue = _evaluator.evaluateExpression(game, cardAffected);
        return _rememberedValue;
    }
}

