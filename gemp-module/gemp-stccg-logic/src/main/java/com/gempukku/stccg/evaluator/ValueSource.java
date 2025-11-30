package com.gempukku.stccg.evaluator;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.game.DefaultGame;

@JsonDeserialize(using = ValueSourceDeserializer.class)
public abstract class ValueSource {
    protected abstract Evaluator getEvaluator(ActionContext actionContext);

    public float getMinimum(DefaultGame cardGame, ActionContext actionContext) {
        return getEvaluator(actionContext).evaluateExpression(cardGame);
    }

    public float getMaximum(DefaultGame cardGame, ActionContext actionContext) {
        return getEvaluator(actionContext).evaluateExpression(cardGame);
    }

    public float evaluateExpression(DefaultGame cardGame, ActionContext actionContext) {
        return getEvaluator(actionContext).evaluateExpression(cardGame);
    }

}