package com.gempukku.stccg.modifiers.attributes;

import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.actions.ActionCardResolver;
import com.gempukku.stccg.actions.FixedCardResolver;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.condition.Condition;
import com.gempukku.stccg.evaluator.ConstantEvaluator;
import com.gempukku.stccg.evaluator.Evaluator;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.AbstractModifier;
import com.gempukku.stccg.modifiers.ModifierEffect;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class AttributeModifier extends AbstractModifier {
    protected final Evaluator _evaluator;
    private final List<CardAttribute> _attributes = new LinkedList<>();

    public AttributeModifier(PhysicalCard modifierSource, Filterable affectFilter, Condition condition,
                             Evaluator evaluator, CardAttribute attribute,
                             ModifierEffect effectType) {
        super(modifierSource, affectFilter, condition, effectType);
        _evaluator = evaluator;
        _attributes.add(attribute);
    }

    public AttributeModifier(PhysicalCard performingCard, ActionCardResolver affectedCards, Condition condition,
                             Evaluator evaluator, CardAttribute attribute,
                             ModifierEffect effectType) {
        super(performingCard, affectedCards, condition, effectType);
        _evaluator = evaluator;
        _attributes.add(attribute);
    }

    public AttributeModifier(PhysicalCard performingCard, PhysicalCard affectedCard,
                             Condition condition, int modifierValue, ModifierEffect effectType,
                             CardAttribute... attributes) {
        super(performingCard, new FixedCardResolver(affectedCard), condition, effectType);
        _evaluator = new ConstantEvaluator(modifierValue);
        _attributes.addAll(Arrays.asList(attributes));
    }

    public AttributeModifier(PhysicalCard modifierSource, Filterable affectFilter, Condition condition,
                             Evaluator evaluator, ModifierEffect effectType) {
        super(modifierSource, affectFilter, condition, effectType);
        _evaluator = evaluator;
        _attributes.add(CardAttribute.STRENGTH);
        _attributes.add(CardAttribute.CUNNING);
        _attributes.add(CardAttribute.INTEGRITY);
    }

    public AttributeModifier(PhysicalCard modifierSource, ActionCardResolver resolver, Condition condition,
                             Evaluator evaluator, ModifierEffect effectType) {
        super(modifierSource, resolver, condition, effectType);
        _evaluator = evaluator;
        _attributes.add(CardAttribute.STRENGTH);
        _attributes.add(CardAttribute.CUNNING);
        _attributes.add(CardAttribute.INTEGRITY);
    }


    @Override
    public String getCardInfoText(DefaultGame cardGame, PhysicalCard affectedCard) {
        String attributeString;
        if (getModifierEffect() == ModifierEffect.ALL_ATTRIBUTE_MODIFIER) {
            attributeString = "All attributes";
        } else {
            attributeString = _attributes.getFirst().toString();
        }

        return attributeString +  " " + TextUtils.signed(_evaluator.evaluateExpression(cardGame)) +
                " from " + _cardSource.getCardLink();
    }

    @Override
    public int getAttributeModifier(DefaultGame cardGame, PhysicalCard physicalCard) {
        return _evaluator.evaluateExpression(cardGame);
    }

    public List<CardAttribute> getAttributesModified() {
        return _attributes;
    }

}