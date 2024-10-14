package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.LimitCounter;
import com.gempukku.stccg.modifiers.ModifiersQuerying;

import java.util.HashMap;
import java.util.Map;

public class CardAffectedPhaseLimitEvaluator extends Evaluator {
    private final Map<Integer, Integer> _evaluatedForCard = new HashMap<>();

    private final String prefix;
    private final Evaluator evaluator;

    private final PhysicalCard _source;
    private final Phase _phase;
    private final int limit;

    public CardAffectedPhaseLimitEvaluator(ActionContext context, int limit, String prefix, Evaluator evaluator) {
        super(context);
        _source = context.getSource();
        _phase = context.getGameState().getCurrentPhase();
        this.limit = limit;
        this.prefix = prefix;
        this.evaluator = evaluator;
    }

    private int evaluateOnce(PhysicalCard cardAffected) {
        ModifiersQuerying modifiersQuerying = _game.getModifiersQuerying();
        LimitCounter limitCounter = modifiersQuerying
                .getUntilEndOfPhaseLimitCounter(_source, prefix + cardAffected.getCardId() + "_", _phase);
        int internalResult = evaluator.evaluateExpression(_game, cardAffected);
        return limitCounter.incrementToLimit(limit, internalResult);
    }

    @Override
    public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
        Integer value = _evaluatedForCard.get(cardAffected.getCardId());
        if (value == null) {
            value = evaluateOnce(cardAffected);
            _evaluatedForCard.put(cardAffected.getCardId(), value);
        }
        return value;
    }
}