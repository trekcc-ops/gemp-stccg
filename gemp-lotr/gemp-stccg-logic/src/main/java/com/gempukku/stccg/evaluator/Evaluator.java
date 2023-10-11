package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.cards.ValueSource;
import com.gempukku.stccg.game.DefaultGame;

public interface Evaluator extends ValueSource {
    int evaluateExpression(DefaultGame game, PhysicalCard cardAffected);
    @Override
    default Evaluator getEvaluator(ActionContext actionContext) { return this; }
}
