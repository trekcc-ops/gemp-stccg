package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.cards.ActionContext;

public class ForEachInMemoryValueSource extends ValueSource {

    private final String _memory;
    private final int _limit;

    public ForEachInMemoryValueSource(String memory, int limit) {
        _memory = memory;
        _limit = limit;
    }
    @Override
    protected Evaluator getEvaluator(ActionContext actionContext) {
        final int count = actionContext.getCardsFromMemory(_memory).size();
        return new ConstantEvaluator(Math.min(_limit, count));
    }
}