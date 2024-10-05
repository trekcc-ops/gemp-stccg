package com.gempukku.stccg.modifiers.attributes;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.condition.Condition;
import com.gempukku.stccg.evaluator.ConstantEvaluator;
import com.gempukku.stccg.evaluator.Evaluator;
import com.gempukku.stccg.modifiers.ModifierEffect;

public class AllAttributeModifier extends AttributeModifier {

    public AllAttributeModifier(PhysicalCard modifierSource, Filterable affectFilter, Condition condition, int modifier) {
        this(modifierSource, affectFilter, condition, new ConstantEvaluator(modifierSource.getGame(), modifier), false);
    }

    public AllAttributeModifier(PhysicalCard modifierSource, Filterable affectFilter, Condition condition, Evaluator evaluator,
                                boolean cumulative) {
        super(modifierSource, affectFilter, condition, evaluator, ModifierEffect.ALL_ATTRIBUTE_MODIFIER);
    }
}
