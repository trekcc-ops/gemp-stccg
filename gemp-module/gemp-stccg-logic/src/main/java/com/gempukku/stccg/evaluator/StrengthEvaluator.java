package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class StrengthEvaluator extends Evaluator {

    private final PhysicalCard _cardWithStrength;

    public StrengthEvaluator(PhysicalCard cardWithStrength) {
        super();
        _cardWithStrength = cardWithStrength;
    }

    @Override
    public int evaluateExpression(DefaultGame cardGame) {
        return _cardWithStrength.getStrength(cardGame);
    }
}