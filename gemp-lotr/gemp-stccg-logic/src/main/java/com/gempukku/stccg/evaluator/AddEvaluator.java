package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class AddEvaluator implements Evaluator {
    private final Evaluator _source;
    private final int _additional;

    public AddEvaluator(int additional, Evaluator source) {
        _additional = additional;
        _source = source;
    }

    @Override
    public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
        return _additional + _source.evaluateExpression(game, cardAffected);
    }
}
