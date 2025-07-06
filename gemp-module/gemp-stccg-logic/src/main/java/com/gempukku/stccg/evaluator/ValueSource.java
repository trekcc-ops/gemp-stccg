package com.gempukku.stccg.evaluator;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

@JsonDeserialize(using = ValueSourceDeserializer.class)
public interface ValueSource {
    Evaluator getEvaluator(ActionContext actionContext);

    default float getMinimum(ActionContext actionContext) {
        return getEvaluator(actionContext).evaluateExpression(actionContext.getGame());
    }

    default float getMaximum(ActionContext actionContext) {
        return getEvaluator(actionContext).evaluateExpression(actionContext.getGame());
    }

    default float evaluateExpression(ActionContext actionContext) {
        return getEvaluator(actionContext).evaluateExpression(actionContext.getGame());
    }

}