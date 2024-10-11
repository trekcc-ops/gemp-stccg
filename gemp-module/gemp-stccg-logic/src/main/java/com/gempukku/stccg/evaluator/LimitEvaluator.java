package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class LimitEvaluator extends Evaluator {
    private final ValueSource _limit;
    private final ValueSource _value;
    private final ActionContext _actionContext;

    public LimitEvaluator(ActionContext actionContext, ValueSource value, ValueSource limit) {
        super(actionContext);
        _actionContext = actionContext;
        _value = value;
        _limit = limit;
    }

    @Override
    public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
        return Math.min( _limit.evaluateExpression(_actionContext, cardAffected),
                _value.evaluateExpression(_actionContext, cardAffected));
    }
}
