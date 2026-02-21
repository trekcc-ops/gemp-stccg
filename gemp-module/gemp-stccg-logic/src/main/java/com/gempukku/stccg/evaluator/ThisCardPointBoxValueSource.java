package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class ThisCardPointBoxValueSource implements SingleValueSource {
    @Override
    public int evaluateExpression(DefaultGame cardGame, GameTextContext actionContext) {
        PhysicalCard thisCard = actionContext.card();
        return thisCard.getPointBoxValue();
    }
}