package com.gempukku.stccg.modifiers.attributes;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.condition.Condition;
import com.gempukku.stccg.evaluator.ConstantEvaluator;
import com.gempukku.stccg.evaluator.Evaluator;
import com.gempukku.stccg.modifiers.ModifierEffect;

public class StrengthModifier extends AttributeModifier {

    public StrengthModifier(ActionContext context, Filterable affectFilter, int modifier) {
        this(context, affectFilter, null, new ConstantEvaluator(context.getGame(), modifier));
    }

    public StrengthModifier(PhysicalCard modifierSource, Filterable affectFilter, Condition condition, int modifier,
                            boolean cumulative) {
        this(modifierSource, affectFilter, condition, new ConstantEvaluator(modifierSource.getGame(), modifier), cumulative);
    }

    public StrengthModifier(ActionContext context, Filterable affectFilter, Condition condition, Evaluator evaluator) {
        this(context.getSource(), affectFilter, condition, evaluator, false);
    }

    public StrengthModifier(PhysicalCard modifierSource, Filterable affectFilter, Condition condition, Evaluator evaluator,
                            boolean cumulative) {
        super(modifierSource, affectFilter, condition, evaluator, cumulative, CardAttribute.STRENGTH, ModifierEffect.STRENGTH_MODIFIER);
    }
}
