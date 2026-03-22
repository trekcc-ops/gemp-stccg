package com.gempukku.stccg.modifiers.attributes;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.evaluator.ConstantEvaluator;
import com.gempukku.stccg.evaluator.Evaluator;
import com.gempukku.stccg.modifiers.ModifierEffect;
import com.gempukku.stccg.requirement.Condition;

public class StrengthModifier extends AttributeModifier {

    public StrengthModifier(PhysicalCard modifierSource, Filterable affectFilter, Condition condition, int modifier) {
        this(modifierSource, affectFilter, condition, new ConstantEvaluator(modifier));
    }

    public StrengthModifier(PhysicalCard modifierSource, Filterable affectFilter, Condition condition, int modifier,
                            boolean cumulative) {
        this(modifierSource, affectFilter, condition, new ConstantEvaluator(modifier));
    }


    public StrengthModifier(PhysicalCard modifierSource, Filterable affectFilter, Condition condition, Evaluator evaluator) {
        super(modifierSource, affectFilter, condition, evaluator, CardAttribute.STRENGTH,
                ModifierEffect.ATTRIBUTE_MODIFIER);
    }
}