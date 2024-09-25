package com.gempukku.stccg.modifiers.attributes;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.condition.Condition;
import com.gempukku.stccg.evaluator.Evaluator;
import com.gempukku.stccg.modifiers.AbstractModifier;
import com.gempukku.stccg.modifiers.ModifierEffect;
import com.gempukku.stccg.TextUtils;

import java.util.LinkedList;
import java.util.List;

public class AttributeModifier extends AbstractModifier {
    protected final Evaluator _evaluator;
    private final List<CardAttribute> _attributes = new LinkedList<>();

    public AttributeModifier(PhysicalCard modifierSource, Filterable affectFilter, Condition condition,
                             Evaluator evaluator, boolean cumulative, CardAttribute attribute,
                             ModifierEffect effectType) {
        super(modifierSource, affectFilter, condition, effectType);
        _evaluator = evaluator;
        _attributes.add(attribute);
    }

    public AttributeModifier(PhysicalCard modifierSource, Filterable affectFilter, Condition condition,
                             Evaluator evaluator, boolean cumulative, ModifierEffect effectType) {
        super(modifierSource, affectFilter, condition, effectType);
        _evaluator = evaluator;
        _attributes.add(CardAttribute.STRENGTH);
        _attributes.add(CardAttribute.CUNNING);
        _attributes.add(CardAttribute.INTEGRITY);
    }

    @Override
    public String getCardInfoText(PhysicalCard affectedCard) {
        String attributeString;
        if (getModifierEffect() == ModifierEffect.ALL_ATTRIBUTE_MODIFIER) {
            attributeString = "All attributes";
        } else {
            attributeString = _attributes.getFirst().toString();
        }

        return attributeString +  " " + TextUtils.signed(_evaluator.evaluateExpression(_game, affectedCard)) +
                " from " + _cardSource.getCardLink();
    }

    @Override
    public int getAttributeModifier(PhysicalCard physicalCard) {
        return _evaluator.evaluateExpression(_game, physicalCard);
    }

}
