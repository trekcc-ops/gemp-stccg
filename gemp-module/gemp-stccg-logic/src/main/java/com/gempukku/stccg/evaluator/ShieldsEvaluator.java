package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.cards.CardWithHullIntegrity;
import com.gempukku.stccg.game.DefaultGame;

public class ShieldsEvaluator extends Evaluator {
    private final CardWithHullIntegrity _cardWithShields;

    public ShieldsEvaluator(CardWithHullIntegrity cardWithShields) {
        super();
        _cardWithShields = cardWithShields;
    }

    public float evaluateExpression(DefaultGame cardGame) {
        return _cardWithShields.getShields(cardGame);
    }
}