package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.cards.CardWithHullIntegrity;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class ShieldsEvaluator extends Evaluator {
    private final CardWithHullIntegrity _cardWithShields;

    public ShieldsEvaluator(CardWithHullIntegrity cardWithShields) {
        super();
        _cardWithShields = cardWithShields;
    }

    public int evaluateExpression(DefaultGame cardGame) {
        return _cardWithShields.getShields(cardGame);
    }

    @Override
    public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
        return evaluateExpression(game);
    }
}