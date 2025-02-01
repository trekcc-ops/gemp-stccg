package com.gempukku.stccg.modifiers.attributes;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.condition.Condition;
import com.gempukku.stccg.condition.TrueCondition;
import com.gempukku.stccg.evaluator.ConstantEvaluator;
import com.gempukku.stccg.evaluator.Evaluator;
import com.gempukku.stccg.modifiers.ModifierEffect;

public class StrengthModifier extends AttributeModifier {

    public StrengthModifier(ActionContext context, Filterable affectFilter, int modifier) {
        this(context, affectFilter, null, new ConstantEvaluator(modifier));
    }

    public StrengthModifier(PhysicalCard modifierSource, Condition condition, int modifier) {
        this(modifierSource, modifierSource, condition, new ConstantEvaluator(modifier));
    }

    public StrengthModifier(PhysicalCard modifierSource, Filterable affectFilter, int modifier) {
        this(modifierSource, affectFilter, new TrueCondition(), new ConstantEvaluator(modifier));
    }

    public StrengthModifier(PhysicalCard modifierSource, Filterable affectFilter, Condition condition, int modifier) {
        this(modifierSource, affectFilter, condition, new ConstantEvaluator(modifier));
    }

    public StrengthModifier(ActionContext context, Filterable affectFilter, Condition condition, Evaluator evaluator) {
        this(context.getSource(), affectFilter, condition, evaluator);
    }

    public StrengthModifier(PhysicalCard modifierSource, Filterable affectFilter, Condition condition, Evaluator evaluator) {
        super(modifierSource, affectFilter, condition, evaluator, CardAttribute.STRENGTH,
                ModifierEffect.STRENGTH_MODIFIER);
    }
}