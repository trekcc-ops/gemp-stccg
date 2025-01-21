package com.gempukku.stccg.cards;

import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.evaluator.Evaluator;
import com.gempukku.stccg.game.DefaultGame;

public class ConstantValueSource implements ValueSource {
    private final int _value;
    public ConstantValueSource(int value) { _value = value; }
    public Evaluator getEvaluator(ActionContext actionContext) {
        return new Evaluator() {
            @Override
            public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
                return _value;
            }
        };
    }

    public int getMinimum(ActionContext actionContext) { return _value; }

    public int getMaximum(ActionContext actionContext) { return _value; }
}