package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.condition.Condition;

public class ConditionEvaluator extends Evaluator {
    private final int _default;
    private final int _conditionFulfilled;
    private final Condition _condition;

    public ConditionEvaluator(ActionContext context, int defaultValue, int conditionFulfilled, Condition condition) {
        super(context);
        _default = defaultValue;
        _conditionFulfilled = conditionFulfilled;
        _condition = condition;
    }

    @Override
    public int evaluateExpression(DefaultGame game, PhysicalCard self) {
        if (_condition.isFulfilled())
            return _conditionFulfilled;
        return _default;
    }
}
