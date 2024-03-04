package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

import java.util.HashMap;
import java.util.Map;

public class PerCardMemoryEvaluator extends Evaluator {
    private final Map<Integer, Integer> _rememberedValue = new HashMap<>();
    private final Evaluator _evaluator;

    public PerCardMemoryEvaluator(Evaluator evaluator) {
        super(evaluator.getGame());
        _evaluator = evaluator;
    }

    @Override
    public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
        Integer value = _rememberedValue.get(cardAffected.getCardId());
        if (value == null) {
            value = _evaluator.evaluateExpression(cardAffected.getGame(), cardAffected);
            _rememberedValue.put(cardAffected.getCardId(), value);
        }
        return value;
    }
}
