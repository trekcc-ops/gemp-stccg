package com.gempukku.lotro.modifiers.lotronly;

import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.modifiers.AbstractModifier;
import com.gempukku.lotro.modifiers.ModifierEffect;
import com.gempukku.lotro.evaluator.ConstantEvaluator;
import com.gempukku.lotro.evaluator.Evaluator;

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
