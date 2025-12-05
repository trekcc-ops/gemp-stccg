package com.gempukku.stccg.modifiers.attributes;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.evaluator.ConstantEvaluator;
import com.gempukku.stccg.evaluator.Evaluator;
import com.gempukku.stccg.modifiers.ModifierEffect;
import com.gempukku.stccg.requirement.Condition;

import java.util.List;

public class AllPersonnelAttributeModifier extends AttributeModifier {

    public AllPersonnelAttributeModifier(PhysicalCard modifierSource, Filterable affectFilter, Condition condition,
                                         int modifier) {
        this(modifierSource, affectFilter, condition, new ConstantEvaluator(modifier), false);
    }

    public AllPersonnelAttributeModifier(PhysicalCard modifierSource, Filterable affectFilter, Condition condition,
                                         Evaluator evaluator, boolean cumulative) {
        super(modifierSource, affectFilter, condition, evaluator, ModifierEffect.ATTRIBUTE_MODIFIER,
                List.of(CardAttribute.INTEGRITY, CardAttribute.CUNNING, CardAttribute.STRENGTH));
    }

}