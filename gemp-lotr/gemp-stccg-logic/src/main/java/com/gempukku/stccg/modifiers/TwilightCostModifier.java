package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.condition.Condition;
import com.gempukku.stccg.evaluator.ConstantEvaluator;
import com.gempukku.stccg.evaluator.Evaluator;

public class TwilightCostModifier extends AbstractModifier {
    private final Evaluator _evaluator;

    public TwilightCostModifier(PhysicalCard source, Filterable affectFilter, int twilightCostModifier) {
        this(source, affectFilter, null, twilightCostModifier);
    }

    public TwilightCostModifier(PhysicalCard source, Filterable affectFilter, Condition condition, int twilightCostModifier) {
        this(source, affectFilter, condition, new ConstantEvaluator(twilightCostModifier));
    }

    public TwilightCostModifier(PhysicalCard source, Filterable affectFilter, Condition condition, Evaluator evaluator) {
        super(source, null, affectFilter, condition, ModifierEffect.TWILIGHT_COST_MODIFIER);
        _evaluator = evaluator;
    }

    @Override
    public String getText(PhysicalCard self) {
        final int value = _evaluator.evaluateExpression(_game, self);
        if (value >= 0)
            return "Twilight cost +" + value;
        else
            return "Twilight cost " + value;
    }

    @Override
    public int getTwilightCostModifier(PhysicalCard physicalCard, PhysicalCard target, boolean ignoreRoamingPenalty) {
        return _evaluator.evaluateExpression(_game, physicalCard);
    }
}
