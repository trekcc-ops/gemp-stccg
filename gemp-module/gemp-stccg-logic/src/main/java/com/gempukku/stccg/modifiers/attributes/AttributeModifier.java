package com.gempukku.stccg.modifiers.attributes;

import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.evaluator.ConstantEvaluator;
import com.gempukku.stccg.evaluator.Evaluator;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.AbstractModifier;
import com.gempukku.stccg.modifiers.ModifierEffect;
import com.gempukku.stccg.requirement.Condition;
import com.gempukku.stccg.requirement.TrueCondition;

import java.util.Arrays;
import java.util.Collection;
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

    public AttributeModifier(PhysicalCard performingCard, CardFilter affectedCards, Condition condition,
                             Evaluator evaluator, CardAttribute attribute,
                             ModifierEffect effectType) {
        super(performingCard, affectedCards, condition, effectType);
        _evaluator = evaluator;
        _attributes.add(attribute);
    }

    public AttributeModifier(PhysicalCard performingCard, CardFilter affectedCardFilter, int modifierValue,
                             Collection<CardAttribute> attributes) {
        super(performingCard, affectedCardFilter, new TrueCondition(), ModifierEffect.ATTRIBUTE_MODIFIER);
        _evaluator = new ConstantEvaluator(modifierValue);
        _attributes.addAll(attributes);
    }


    public AttributeModifier(PhysicalCard performingCard, PhysicalCard affectedCard,
                             Condition condition, int modifierValue, ModifierEffect effectType,
                             CardAttribute... attributes) {
        super(performingCard, Filters.card(affectedCard), condition, effectType);
        _evaluator = new ConstantEvaluator(modifierValue);
        _attributes.addAll(Arrays.asList(attributes));
    }

    public AttributeModifier(PhysicalCard modifierSource, Filterable affectFilter, Condition condition,
                             Evaluator evaluator, ModifierEffect effectType, Collection<CardAttribute> attributes) {
        super(modifierSource, affectFilter, condition, effectType);
        _evaluator = evaluator;
        _attributes.addAll(attributes);
    }


    @Override
    public String getCardInfoText(DefaultGame cardGame, PhysicalCard affectedCard) {
        return _attributes +  " " + TextUtils.signed(_evaluator.evaluateExpression(cardGame)) +
                " from " + _cardSource.getCardLink();
    }

    @Override
    public float getAttributeModifier(DefaultGame cardGame, PhysicalCard physicalCard) {
        return _evaluator.evaluateExpression(cardGame);
    }

    public List<CardAttribute> getAttributesModified() {
        return _attributes;
    }

}