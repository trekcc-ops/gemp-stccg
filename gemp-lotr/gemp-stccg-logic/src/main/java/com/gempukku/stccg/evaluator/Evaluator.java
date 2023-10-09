package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.cards.DefaultActionContext;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.cards.ValueSource;
import com.gempukku.stccg.game.DefaultGame;

public interface Evaluator<AbstractGame extends DefaultGame> extends ValueSource<AbstractGame> {
    int evaluateExpression(AbstractGame game, PhysicalCard cardAffected);
    @Override
    default Evaluator getEvaluator(DefaultActionContext<AbstractGame> actionContext) {
        return this;
    }
}
