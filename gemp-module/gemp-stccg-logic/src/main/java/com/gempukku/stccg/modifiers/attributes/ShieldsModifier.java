package com.gempukku.stccg.modifiers.attributes;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.evaluator.Evaluator;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.modifiers.ModifierEffect;
import com.gempukku.stccg.requirement.Condition;

public class ShieldsModifier extends AttributeModifier {

    public ShieldsModifier(PhysicalCard performingCard, CardFilter affectedCardFilter, Condition condition,
                           Evaluator amount, boolean isCumulative) {
        super(performingCard, affectedCardFilter, condition, amount, CardAttribute.SHIELDS,
                ModifierEffect.ATTRIBUTE_MODIFIER, isCumulative);
    }

}