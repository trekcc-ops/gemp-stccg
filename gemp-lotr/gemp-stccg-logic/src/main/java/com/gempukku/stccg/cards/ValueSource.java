package com.gempukku.stccg.cards;

import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.evaluator.Evaluator;

public interface ValueSource<AbstractGame extends DefaultGame> {
    Evaluator getEvaluator(DefaultActionContext<AbstractGame> actionContext);

    default int getMinimum(DefaultActionContext<AbstractGame> actionContext) {
        return getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
    }

    default int getMaximum(DefaultActionContext<AbstractGame> actionContext) {
        return getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
    }
}
