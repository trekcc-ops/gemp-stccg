package com.gempukku.stccg.modifiers.attributes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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
import com.gempukku.stccg.modifiers.ModifierTimingType;
import com.gempukku.stccg.requirement.Condition;
import com.gempukku.stccg.requirement.TrueCondition;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class AttributeModifier extends AbstractModifier {

    @JsonProperty("evaluator")
    protected final Evaluator _evaluator;

    @JsonProperty("attributes")
    private final List<CardAttribute> _attributes = new LinkedList<>();

    @JsonCreator
    private AttributeModifier(@JsonProperty("evaluator") Evaluator evaluator,
                      @JsonProperty(value = "attributes", required = true) Collection<CardAttribute> attributes,
                      @JsonProperty("performingCard") PhysicalCard performingCard,
                      @JsonProperty("affectedCards") CardFilter affectFilter,
                      @JsonProperty("condition") Condition condition,
                      @JsonProperty("effectType") ModifierEffect effectType) {
        super(performingCard, affectFilter, condition, effectType);
        _evaluator = evaluator;
        _attributes.addAll(attributes);
    }

    public AttributeModifier(PhysicalCard performingCard, CardFilter affectedCards, Condition condition,
                             Evaluator evaluator, Collection<CardAttribute> attributes,
                             ModifierTimingType timingType, boolean cumulative) {
        this(evaluator, attributes, performingCard, affectedCards, condition,
                ModifierEffect.ATTRIBUTE_MODIFIER);
    }


    public AttributeModifier(PhysicalCard modifierSource, Filterable affectFilter, Condition condition,
                             Evaluator evaluator, CardAttribute attribute,
                             ModifierEffect effectType) {
        this(evaluator, List.of(attribute), modifierSource, Filters.changeToFilter(affectFilter), condition,
                effectType);
    }

    public AttributeModifier(PhysicalCard performingCard, CardFilter affectedCards, Condition condition,
                             Evaluator evaluator, CardAttribute attribute,
                             ModifierEffect effectType) {
        this(evaluator, List.of(attribute), performingCard, affectedCards, condition, effectType);
    }

    public AttributeModifier(PhysicalCard performingCard, CardFilter affectedCardFilter, int modifierValue,
                             Collection<CardAttribute> attributes) {
        this(new ConstantEvaluator(modifierValue), attributes, performingCard, affectedCardFilter,
                new TrueCondition(), ModifierEffect.ATTRIBUTE_MODIFIER);
    }




    public AttributeModifier(PhysicalCard performingCard, PhysicalCard affectedCard,
                             Condition condition, int modifierValue, ModifierEffect effectType,
                             CardAttribute... attributes) {
        this(new ConstantEvaluator(modifierValue), List.of(attributes), performingCard, Filters.card(affectedCard),
                condition, effectType);
    }

    public AttributeModifier(PhysicalCard modifierSource, Filterable affectFilter, Condition condition,
                             Evaluator evaluator, ModifierEffect effectType, Collection<CardAttribute> attributes) {
        this(evaluator, attributes, modifierSource, Filters.changeToFilter(affectFilter), condition, effectType);
    }


    @Override
    public String getCardInfoText(DefaultGame cardGame, PhysicalCard affectedCard) {
        return _attributes +  " " + TextUtils.signed(_evaluator.evaluateExpression(cardGame));
    }

    @Override
    public float getAttributeModifier(DefaultGame cardGame, PhysicalCard physicalCard) {
        return _evaluator.evaluateExpression(cardGame);
    }

    @JsonIgnore
    public List<CardAttribute> getAttributesModified() {
        return _attributes;
    }

}