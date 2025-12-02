package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.game.DefaultGame;

public abstract class Evaluator extends ValueSource {
    public abstract float evaluateExpression(DefaultGame cardGame);

    public float evaluateExpression(DefaultGame cardGame, ActionContext actionContext) {
        return evaluateExpression(cardGame);
    }
}