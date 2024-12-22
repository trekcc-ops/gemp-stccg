package com.gempukku.stccg.modifiers.attributes;

import com.gempukku.stccg.actions.ActionCardResolver;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.condition.Condition;
import com.gempukku.stccg.condition.TrueCondition;
import com.gempukku.stccg.evaluator.ConstantEvaluator;
import com.gempukku.stccg.modifiers.ModifierEffect;

public class RangeModifier extends AttributeModifier {

    public RangeModifier(PhysicalCard performingCard, ActionCardResolver affectedCards, int modifierValue) {
        super(performingCard, affectedCards, new TrueCondition(),
                new ConstantEvaluator(performingCard.getGame(), modifierValue),
                CardAttribute.RANGE, ModifierEffect.SHIP_ATTRIBUTE_MODIFIER);
    }

    public RangeModifier(PhysicalCard performingCard, PhysicalCard affectedCard, Condition condition,
                         int modifierValue) {
        super(performingCard, new ActionCardResolver(affectedCard), condition,
                new ConstantEvaluator(performingCard.getGame(), modifierValue), CardAttribute.RANGE,
                ModifierEffect.SHIP_ATTRIBUTE_MODIFIER);
    }

}