package com.gempukku.stccg.modifiers.attributes;

import com.gempukku.stccg.actions.ActionCardResolver;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.evaluator.ConstantEvaluator;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.modifiers.ModifierEffect;
import com.gempukku.stccg.requirement.Condition;
import com.gempukku.stccg.requirement.TrueCondition;

public class RangeModifier extends AttributeModifier {

    public RangeModifier(PhysicalCard performingCard, ActionCardResolver affectedCards, int modifierValue) {
        super(performingCard, affectedCards, new TrueCondition(), new ConstantEvaluator(modifierValue),
                CardAttribute.RANGE, ModifierEffect.ATTRIBUTE_MODIFIER);
    }

    public RangeModifier(PhysicalCard performingCard, PhysicalCard affectedCard, Condition condition,
                         int modifierValue) {
        super(performingCard, Filters.card(affectedCard), condition,
                new ConstantEvaluator(modifierValue), CardAttribute.RANGE,
                ModifierEffect.ATTRIBUTE_MODIFIER);
    }

}