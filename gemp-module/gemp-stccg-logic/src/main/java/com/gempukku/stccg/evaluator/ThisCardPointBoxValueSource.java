package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

public class ThisCardPointBoxValueSource implements SingleValueSource {
    @Override
    public int evaluateExpression(DefaultGame cardGame, ActionContext actionContext)
            throws InvalidGameLogicException {
        PhysicalCard thisCard = actionContext.card();
        return thisCard.getPointBoxValue();
    }
}