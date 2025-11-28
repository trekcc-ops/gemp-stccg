package com.gempukku.stccg.evaluator;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

@JsonDeserialize(using = ValueSourceDeserializer.class)
public interface ValueSource {
    Evaluator getEvaluator(ActionContext actionContext);

    default float getMinimum(DefaultGame cardGame, ActionContext actionContext) {
        return getEvaluator(actionContext).evaluateExpression(cardGame);
    }

    default float getMaximum(DefaultGame cardGame, ActionContext actionContext) {
        return getEvaluator(actionContext).evaluateExpression(cardGame);
    }

    default float evaluateExpression(DefaultGame cardGame, ActionContext actionContext) {
        return getEvaluator(actionContext).evaluateExpression(cardGame);
    }

}