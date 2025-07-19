package com.gempukku.stccg.modifiers.attributes;

import com.gempukku.stccg.actions.ActionCardResolver;
import com.gempukku.stccg.actions.FixedCardResolver;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.condition.Condition;
import com.gempukku.stccg.condition.TrueCondition;
import com.gempukku.stccg.evaluator.ConstantEvaluator;
import com.gempukku.stccg.evaluator.Evaluator;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.modifiers.ModifierEffect;

public class ShieldsModifier extends AttributeModifier {

    public ShieldsModifier(PhysicalCard performingCard, CardFilter affectedCards, Evaluator modifierAmount) {
        super(performingCard, affectedCards, new TrueCondition(), modifierAmount,
                CardAttribute.SHIELDS, ModifierEffect.SHIP_ATTRIBUTE_MODIFIER);
    }

}