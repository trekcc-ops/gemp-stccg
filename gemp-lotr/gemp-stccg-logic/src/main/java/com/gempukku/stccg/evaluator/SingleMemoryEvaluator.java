package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class SingleMemoryEvaluator extends Evaluator {
    private Integer _rememberedValue;
    private final Evaluator _evaluator;

    public SingleMemoryEvaluator(ActionContext actionContext, Evaluator evaluator) {
        super(actionContext);
        _evaluator = evaluator;
    }

    @Override
    public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
        if (_rememberedValue == null)
            _rememberedValue = _evaluator.evaluateExpression(_game, cardAffected);
        return _rememberedValue;
    }
}

