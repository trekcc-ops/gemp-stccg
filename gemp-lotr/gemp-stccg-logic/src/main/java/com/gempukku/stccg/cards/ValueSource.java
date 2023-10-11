package com.gempukku.stccg.cards;

import com.gempukku.stccg.evaluator.Evaluator;

public interface ValueSource {
    Evaluator getEvaluator(ActionContext actionContext);

    default int getMinimum(ActionContext actionContext) {
        return getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
    }

    default int getMaximum(ActionContext actionContext) {
        return getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
    }
}
