package com.gempukku.stccg.evaluator;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.evaluator.Evaluator;
import com.gempukku.stccg.game.PlayerNotFoundException;
import com.gempukku.stccg.processes.StartOfTurnGameProcess;
import com.gempukku.stccg.processes.st1e.*;

@JsonDeserialize(using = ValueSourceDeserializer.class)
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