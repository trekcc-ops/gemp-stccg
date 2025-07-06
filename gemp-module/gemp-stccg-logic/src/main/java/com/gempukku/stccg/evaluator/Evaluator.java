package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public abstract class Evaluator implements ValueSource {
    @Override
    public Evaluator getEvaluator(ActionContext actionContext) { return this; }
    public abstract int evaluateExpression(DefaultGame cardGame);
}