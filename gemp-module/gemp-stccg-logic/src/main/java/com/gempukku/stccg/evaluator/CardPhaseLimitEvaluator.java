package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.ValueSource;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.LimitCounter;

public class CardPhaseLimitEvaluator extends Evaluator {
    private Integer _evaluated;

    private final PhysicalCard _source;
    private final Phase _phase;
    private final ValueSource _amount, _limit;
    private final ActionContext _context;

    public CardPhaseLimitEvaluator(ActionContext context, ValueSource limit, ValueSource amount) {
        super(context);
        _context = context;
        _source = context.getSource();
        _phase = context.getGameState().getCurrentPhase();
        _limit = limit;
        _amount = amount;
    }

    private int evaluateOnce(PhysicalCard cardAffected) {
        LimitCounter limitCounter = _game.getModifiersQuerying().getUntilEndOfPhaseLimitCounter(_source, _phase);
        int amountResult = _amount.evaluateExpression(_context, cardAffected);
        int limitResult = _limit.evaluateExpression(_context, cardAffected);
        return limitCounter.incrementToLimit(limitResult, amountResult);
    }

    @Override
    public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
        if (_evaluated == null)
            _evaluated = evaluateOnce(cardAffected);
        return _evaluated;
    }
}
