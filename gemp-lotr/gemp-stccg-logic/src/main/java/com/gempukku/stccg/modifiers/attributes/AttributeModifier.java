package com.gempukku.stccg.modifiers.attributes;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.condition.Condition;
import com.gempukku.stccg.evaluator.Evaluator;
import com.gempukku.stccg.modifiers.AbstractModifier;
import com.gempukku.stccg.modifiers.ModifierEffect;
import com.gempukku.stccg.rules.TextUtils;

public class AttributeModifier extends AbstractModifier {
    protected final Evaluator _evaluator;
    private final CardAttribute _attribute;

        // TODO - The "cumulative" value is never used
    public AttributeModifier(PhysicalCard modifierSource, Filterable affectFilter, Condition condition,
                             Evaluator evaluator, boolean cumulative, CardAttribute attribute,
                             ModifierEffect effectType) {
        super(modifierSource, affectFilter, condition, effectType);
        _evaluator = evaluator;
        _attribute = attribute;
    }

    @Override
    public String getCardInfoText(PhysicalCard affectedCard) {
        return _attribute.toString() +  " " + TextUtils.signed(_evaluator.evaluateExpression(_game, affectedCard)) +
                " from " + _cardSource.getCardLink();
    }

    @Override
    public int getStrengthModifier(PhysicalCard physicalCard) {
        return _evaluator.evaluateExpression(_game, physicalCard);
    }

}
