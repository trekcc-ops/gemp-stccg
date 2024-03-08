package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class ConstantEvaluator extends Evaluator {
    private final int _value;

    public ConstantEvaluator(DefaultGame game, int value) {
            // TODO - I don't love passing this null, but realistically ConstantEvaluators don't need an action context
        super(game);
        _value = value;
    }

    public ConstantEvaluator(ActionContext context, int value) {
        super(context);
        _value = value;
    }

    @Override
    public int evaluateExpression(DefaultGame game, PhysicalCard self) {
        return _value;
    }
}
