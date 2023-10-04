package com.gempukku.lotro.evaluator;

import com.gempukku.lotro.cards.DefaultActionContext;
import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.cards.ValueSource;
import com.gempukku.lotro.game.DefaultGame;

public interface Evaluator<AbstractGame extends DefaultGame> extends ValueSource<AbstractGame> {
    int evaluateExpression(AbstractGame game, PhysicalCard cardAffected);
    @Override
    default Evaluator getEvaluator(DefaultActionContext<AbstractGame> actionContext) {
        return this;
    }
}
