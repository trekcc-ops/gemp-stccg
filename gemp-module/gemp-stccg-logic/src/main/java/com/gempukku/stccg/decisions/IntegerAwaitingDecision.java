package com.gempukku.stccg.decisions;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.common.AwaitingDecisionType;
import com.gempukku.stccg.common.DecisionResultInvalidException;

public class IntegerAwaitingDecision extends AbstractAwaitingDecision {
    private final Integer _min;
    private final Integer _max;
    protected final ActionContext _context;
    protected final String _memoryId;

    public IntegerAwaitingDecision(ActionContext context, int id, String text, Integer min, Integer max,
                                   Integer defaultValue, String memoryId) {
        super(id, text, AwaitingDecisionType.INTEGER);
        _min = min;
        _max = max;
        _context = context;
        _memoryId = memoryId;
        if (min != null)
            setParam("min", min.toString());
        if (max != null)
            setParam("max", max.toString());
        if (defaultValue != null)
            setParam("defaultValue", defaultValue.toString());
    }

    public void setDefaultValue(int defaultValue) {
        setParam("defaultValue", String.valueOf(defaultValue));
    }

    protected int getValidatedResult(String result) throws DecisionResultInvalidException {
        try {
            int value = Integer.parseInt(result);
            if (_min != null && _min > value)
                throw new DecisionResultInvalidException();
            if (_max != null && _max < value)
                throw new DecisionResultInvalidException();

            return value;
        } catch (NumberFormatException exp) {
            throw new DecisionResultInvalidException();
        }
    }

    @Override
    public void decisionMade(String result) throws DecisionResultInvalidException {
        _context.setValueToMemory(_memoryId, String.valueOf(getValidatedResult(result)));
    }

    public ActionContext getActionContext() { return _context; }
}