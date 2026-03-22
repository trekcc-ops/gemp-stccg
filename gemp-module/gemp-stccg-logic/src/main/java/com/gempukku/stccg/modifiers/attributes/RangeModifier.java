package com.gempukku.stccg.modifiers.attributes;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.evaluator.ConstantEvaluator;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.modifiers.ModifierEffect;
import com.gempukku.stccg.requirement.Condition;

public class RangeModifier extends AttributeModifier {

    public RangeModifier(PhysicalCard performingCard, PhysicalCard affectedCard, Condition condition,
                         int modifierValue) {
        super(performingCard, Filters.card(affectedCard), condition,
                new ConstantEvaluator(modifierValue), CardAttribute.RANGE,
                ModifierEffect.ATTRIBUTE_MODIFIER);
    }

    public RangeModifier(PhysicalCard performingCard, CardFilter affectedCardFilter, Condition condition,
                         int modifierValue, boolean isCumulative) {
        super(performingCard, affectedCardFilter, condition,
                new ConstantEvaluator(modifierValue), CardAttribute.RANGE,
                ModifierEffect.ATTRIBUTE_MODIFIER, isCumulative);
    }

}