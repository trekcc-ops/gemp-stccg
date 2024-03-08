package com.gempukku.stccg.modifiers.attributes;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.condition.Condition;
import com.gempukku.stccg.evaluator.ConstantEvaluator;
import com.gempukku.stccg.modifiers.ModifierEffect;

public class CunningModifier extends AttributeModifier {

    public CunningModifier(PhysicalCard modifierSource, Filterable affectFilter, Condition condition, int modifier,
                           boolean cumulative) {
        super(modifierSource, affectFilter, condition, new ConstantEvaluator(modifierSource.getGame(), modifier),
                cumulative, CardAttribute.CUNNING, ModifierEffect.CUNNING_MODIFIER);
    }

}
