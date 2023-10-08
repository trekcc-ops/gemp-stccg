package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class ForEachThreatEvaluator implements Evaluator {
    @Override
    public int evaluateExpression(DefaultGame game, PhysicalCard self) {
        return game.getGameState().getThreats();
    }
}
