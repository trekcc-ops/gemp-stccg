package com.gempukku.lotro.modifiers.lotronly;

import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.modifiers.AbstractModifier;
import com.gempukku.lotro.modifiers.ModifierEffect;
import com.gempukku.lotro.condition.Condition;
import com.gempukku.lotro.evaluator.ConstantEvaluator;
import com.gempukku.lotro.evaluator.Evaluator;

public class InitiativeHandSizeModifier extends AbstractModifier {
    private final Evaluator _evaluator;

    public InitiativeHandSizeModifier(PhysicalCard source, int modifier) {
        this(source, null, modifier);
    }

    public InitiativeHandSizeModifier(PhysicalCard source, Condition condition, int modifier) {
        this(source, condition, new ConstantEvaluator(modifier));
    }

    public InitiativeHandSizeModifier(PhysicalCard source, Condition condition, Evaluator evaluator) {
        super(source, null, null, condition, ModifierEffect.INITIATIVE_MODIFIER);
        _evaluator = evaluator;
    }

    @Override
    public int getInitiativeHandSizeModifier(DefaultGame game) {
        return _evaluator.evaluateExpression(game, null);
    }
}
