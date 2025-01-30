package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class MultiplyEvaluator extends Evaluator {
    private final Evaluator _source;
    private final Evaluator _multiplier;
    private final DefaultGame _game;

    public MultiplyEvaluator(ActionContext context, Evaluator multiplier, Evaluator source) {
        super();
        _multiplier = multiplier;
        _game = context.getGame();
        _source = source;
    }

    public MultiplyEvaluator(ActionContext context, int multiplier, Evaluator source) {
        this(context, new ConstantValueSource(multiplier).getEvaluator(context), source);
    }

    @Override
    public int evaluateExpression(DefaultGame game, PhysicalCard self) {
        return _multiplier.evaluateExpression(_game, self) * _source.evaluateExpression(_game, self);
    }
}