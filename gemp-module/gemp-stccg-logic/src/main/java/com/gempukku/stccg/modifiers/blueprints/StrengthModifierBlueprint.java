package com.gempukku.stccg.modifiers.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.FilterFactory;
import com.gempukku.stccg.cards.blueprints.FilterableSource;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.condition.RequirementCondition;
import com.gempukku.stccg.evaluator.Evaluator;
import com.gempukku.stccg.evaluator.ValueSource;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.attributes.StrengthModifier;

import java.util.List;

public class StrengthModifierBlueprint implements ModifierBlueprint {

    private final FilterableSource _filterableSource;
    private final List<Requirement> _requirements;
    private final ValueSource _valueSource;

    StrengthModifierBlueprint(@JsonProperty(value = "amount", required = true)
                              ValueSource amount,
                              @JsonProperty(value = "filter", required = true)
                              String filterText,
                              @JsonProperty(value = "requires", required = true)
                              List<Requirement> requirements) throws InvalidCardDefinitionException {
        _valueSource = amount;
        _filterableSource = new FilterFactory().generateFilter(filterText);
        _requirements = requirements;
    }

    public Modifier getModifier(ActionContext actionContext) {
        final Evaluator evaluator = _valueSource.getEvaluator(actionContext);
        return new StrengthModifier(actionContext, _filterableSource.getFilterable(actionContext),
                new RequirementCondition(_requirements, actionContext), evaluator);
    }

}