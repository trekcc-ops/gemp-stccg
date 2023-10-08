package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class NegativeEvaluator implements Evaluator {
    private final Evaluator _source;

    public NegativeEvaluator(Evaluator source) {
        _source = source;
    }

    @Override
    public int evaluateExpression(DefaultGame game, PhysicalCard self) {
        return -_source.evaluateExpression(game, self);
    }
}
