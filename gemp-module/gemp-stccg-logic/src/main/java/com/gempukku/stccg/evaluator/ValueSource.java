package com.gempukku.stccg.evaluator;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

@JsonDeserialize(using = ValueSourceDeserializer.class)
public abstract class ValueSource {

    public float getMinimum(DefaultGame cardGame, ActionContext actionContext) throws InvalidGameLogicException {
        return evaluateExpression(cardGame, actionContext);
    }

    public float getMaximum(DefaultGame cardGame, ActionContext actionContext) throws InvalidGameLogicException {
        return evaluateExpression(cardGame, actionContext);
    }

    public abstract float evaluateExpression(DefaultGame cardGame, ActionContext actionContext)
            throws InvalidGameLogicException;

}