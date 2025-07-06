package com.gempukku.stccg.evaluator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.evaluator.ValueSource;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.evaluator.Evaluator;
import com.gempukku.stccg.game.DefaultGame;

public class ConstantValueSource implements ValueSource {
    private final int _value;

    @JsonCreator
    public ConstantValueSource(int value) { _value = value; }
    public Evaluator getEvaluator(ActionContext actionContext) {
        return new Evaluator() {
            @Override
            public int evaluateExpression(DefaultGame game) {
                return _value;
            }
        };
    }

    public int getMinimum(ActionContext actionContext) { return _value; }

    public int getMaximum(ActionContext actionContext) { return _value; }
}