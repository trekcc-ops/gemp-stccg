package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.evaluator.Evaluator;
import com.gempukku.stccg.game.PlayerNotFoundException;

public interface ValueSource {
    Evaluator getEvaluator(ActionContext actionContext);

    default int getMinimum(ActionContext actionContext) {
        return getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
    }

    default int getMaximum(ActionContext actionContext) {
        return getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
    }

    default int evaluateExpression(ActionContext actionContext) {
        return evaluateExpression(actionContext, actionContext.getSource());
    }

    default int evaluateExpression(ActionContext actionContext, PhysicalCard cardAffected) {
        return getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), cardAffected);
    }

}