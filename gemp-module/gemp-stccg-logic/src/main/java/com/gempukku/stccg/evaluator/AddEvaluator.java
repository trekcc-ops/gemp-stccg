package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class AddEvaluator extends Evaluator {
    private final Evaluator _source;
    private final int _additional;

    public AddEvaluator(ActionContext actionContext, int additional, Evaluator source) {
        super(actionContext);
        _additional = additional;
        _source = source;
    }

    @Override
    public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
        return _additional + _source.evaluateExpression(cardAffected.getGame(), cardAffected);
    }
}
