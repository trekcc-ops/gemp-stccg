package com.gempukku.stccg.modifiers.attributes;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.evaluator.ConstantEvaluator;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.modifiers.ModifierTimingType;
import com.gempukku.stccg.requirement.Condition;

import java.util.List;

import static com.gempukku.stccg.common.filterable.CardAttribute.*;

public class Modifiers {

    public static AttributeModifier allPersonnelAttributes(PhysicalCard performingCard,
                                                                       CardFilter affectedCards,
                                                              Condition condition, int amount,
                                                              ModifierTimingType modifierTiming) {
        return new AttributeModifier(performingCard, affectedCards, condition,
                new ConstantEvaluator(amount),
                List.of(INTEGRITY, CUNNING, STRENGTH),
                modifierTiming,
                false);
    }

}