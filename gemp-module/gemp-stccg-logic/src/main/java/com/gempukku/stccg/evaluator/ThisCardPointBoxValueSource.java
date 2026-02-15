package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

public class ThisCardPointBoxValueSource extends ValueSource {
    @Override
    public float evaluateExpression(DefaultGame cardGame, ActionContext actionContext)
            throws InvalidGameLogicException {
        PhysicalCard thisCard = actionContext.card();
        return thisCard.getPointBoxValue();
    }
}