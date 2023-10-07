package com.gempukku.lotro.evaluator;

import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.condition.Condition;

public class ConditionEvaluator implements Evaluator {
    private final int _default;
    private final int _conditionFulfilled;
    private final Condition _condition;

    public ConditionEvaluator(int aDefault, int conditionFulfilled, Condition condition) {
        _default = aDefault;
        _conditionFulfilled = conditionFulfilled;
        _condition = condition;
    }

    @Override
    public int evaluateExpression(DefaultGame game, PhysicalCard self) {
        if (_condition.isFulfilled(game))
            return _conditionFulfilled;
        return _default;
    }
}
