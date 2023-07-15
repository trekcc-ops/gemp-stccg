package com.gempukku.lotro.game.modifiers.evaluator;

import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.game.DefaultGame;

public class ForEachBurdenEvaluator implements Evaluator {
    @Override
    public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
        return game.getGameState().getBurdens();
    }
}