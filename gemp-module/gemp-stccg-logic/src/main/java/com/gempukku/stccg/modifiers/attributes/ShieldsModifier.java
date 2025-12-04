package com.gempukku.stccg.modifiers.attributes;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.requirement.TrueCondition;
import com.gempukku.stccg.evaluator.Evaluator;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.modifiers.ModifierEffect;

public class ShieldsModifier extends AttributeModifier {

    public ShieldsModifier(PhysicalCard performingCard, CardFilter affectedCards, Evaluator modifierAmount) {
        super(performingCard, affectedCards, new TrueCondition(), modifierAmount,
                CardAttribute.SHIELDS, ModifierEffect.ATTRIBUTE_MODIFIER);
    }

}