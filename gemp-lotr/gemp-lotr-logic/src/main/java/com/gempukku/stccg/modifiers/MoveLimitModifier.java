package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.evaluator.ConstantEvaluator;
import com.gempukku.stccg.evaluator.Evaluator;

public class MoveLimitModifier extends AbstractModifier {
    private final Evaluator _moveLimitModifier;

    public MoveLimitModifier(PhysicalCard source, int moveLimitModifier) {
        super(source, null, null, null, ModifierEffect.MOVE_LIMIT_MODIFIER);
        _moveLimitModifier = new ConstantEvaluator(moveLimitModifier);
    }

    @Override
    public int getMoveLimitModifier(DefaultGame game) {
        return _moveLimitModifier.evaluateExpression(game, null);
    }
}
