package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;

public class DefaultValueSource implements ValueSource {

    private final int _min;
    private final int _max;

    public DefaultValueSource(String stringValue) throws InvalidCardDefinitionException {
        if (stringValue.contains("-")) {
            final String[] split = stringValue.split("-", 2);
            _min = Integer.parseInt(split[0]);
            _max = Integer.parseInt(split[1]);
            if (_min > _max || _min < 0 || _max < 1)
                throw new InvalidCardDefinitionException("Unable to resolve count: " + stringValue);
        } else {
            _min = Integer.parseInt(stringValue);
            _max = _min;
        }
    }

    @Override
    public Evaluator getEvaluator(ActionContext actionContext) {
        if (_min == _max)
            return new ConstantEvaluator(_min);
        else
            throw new RuntimeException("Evaluator has resolved to range");
    }

    @Override
    public int getMinimum(ActionContext actionContext) {
        return _min;
    }

    @Override
    public int getMaximum(ActionContext actionContext) {
        return _max;
    }

}