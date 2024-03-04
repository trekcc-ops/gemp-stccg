package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class MinEvaluator extends Evaluator {
    private final int _limit;
    private final Evaluator _evaluator;

    public MinEvaluator(Evaluator evaluator, int limit) {
        super(evaluator.getGame());
        _evaluator = evaluator;
        _limit = limit;
    }

    @Override
    public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
        return Math.max(_limit, _evaluator.evaluateExpression(cardAffected.getGame(), cardAffected));
    }
}
