package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class MinEvaluator implements Evaluator {
    private final int _limit;
    private final Evaluator _evaluator;

    public MinEvaluator(Evaluator evaluator, int limit) {
        _evaluator = evaluator;
        _limit = limit;
    }

    @Override
    public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
        return Math.max(_limit, _evaluator.evaluateExpression(game, cardAffected));
    }
}
