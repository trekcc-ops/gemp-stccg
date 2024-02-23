package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.condition.Condition;
import com.gempukku.stccg.evaluator.ConstantEvaluator;
import com.gempukku.stccg.evaluator.Evaluator;

public class ResistanceModifier extends AbstractModifier {
    private final Evaluator evaluator;
    private final boolean nonCardTextModifier;

    public ResistanceModifier(PhysicalCard source, Filterable affectFilter, int modifier) {
        this(source, affectFilter, new ConstantEvaluator(modifier));
    }

    public ResistanceModifier(PhysicalCard source, Filterable affectFilter, Evaluator evaluator) {
        this(source, affectFilter, null, evaluator);
    }

    public ResistanceModifier(PhysicalCard source, Filterable affectFilter, Condition condition, int modifier) {
        this(source, affectFilter, condition, new ConstantEvaluator(modifier));
    }

    public ResistanceModifier(PhysicalCard source, Filterable affectFilter, Condition condition, Evaluator evaluator) {
        this(source, affectFilter, condition, evaluator, false);
    }

    public ResistanceModifier(PhysicalCard source, Filterable affectFilter, Condition condition, Evaluator evaluator, boolean nonCardTextModifier) {
        super(source, null, affectFilter, condition, ModifierEffect.RESISTANCE_MODIFIER);
        this.evaluator = evaluator;
        this.nonCardTextModifier = nonCardTextModifier;
    }

    @Override
    public boolean isNonCardTextModifier() {
        return nonCardTextModifier;
    }

    @Override
    public String getText(PhysicalCard self) {
        int modifier = evaluator.evaluateExpression(_game, self);
        return "Resistance " + ((modifier < 0) ? modifier : ("+" + modifier));
    }

    @Override
    public int getResistanceModifier(PhysicalCard physicalCard) {
        return evaluator.evaluateExpression(_game, physicalCard);
    }
}
