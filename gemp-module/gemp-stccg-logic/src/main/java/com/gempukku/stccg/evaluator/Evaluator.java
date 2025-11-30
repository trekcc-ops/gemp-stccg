package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.game.DefaultGame;

public abstract class Evaluator extends ValueSource {
    protected Evaluator getEvaluator(ActionContext actionContext) { return this; }
    public abstract float evaluateExpression(DefaultGame cardGame);
}