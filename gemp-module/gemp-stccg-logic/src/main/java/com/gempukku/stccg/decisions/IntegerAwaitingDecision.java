package com.gempukku.stccg.decisions;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.common.AwaitingDecisionType;
import com.gempukku.stccg.common.DecisionResultInvalidException;

public abstract class IntegerAwaitingDecision extends AbstractAwaitingDecision {
    private final int _min;
    private final int _max;

    public IntegerAwaitingDecision(ActionContext context, String choiceText, ValueSource valueSource) {
        super(context.getPerformingPlayer(), context.substituteText(choiceText), AwaitingDecisionType.INTEGER);
        _min = valueSource.getMinimum(context);
        _max = valueSource.getMaximum(context);
        setParam("min", _min);
        setParam("max", _max);
    }

    public void setDefaultValue(int defaultValue) {
        setParam("defaultValue", defaultValue);
    }

    protected int getValidatedResult(String result) throws DecisionResultInvalidException {
        try {
            int value = Integer.parseInt(result);
            if (_min > value || _max < value)
                throw new DecisionResultInvalidException();
            return value;
        } catch (NumberFormatException exp) {
            throw new DecisionResultInvalidException();
        }
    }

}