package com.gempukku.stccg.modifiers.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.condition.RequirementCondition;
import com.gempukku.stccg.evaluator.Evaluator;
import com.gempukku.stccg.evaluator.ValueSource;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.attributes.StrengthModifier;

import java.util.List;

public class StrengthModifierBlueprint implements ModifierBlueprint {

    private final FilterBlueprint _filterBlueprint;
    private final List<Requirement> _requirements;
    private final ValueSource _valueSource;

    StrengthModifierBlueprint(@JsonProperty(value = "amount", required = true)
                              ValueSource amount,
                              @JsonProperty(value = "filter", required = true)
                              FilterBlueprint filterBlueprint,
                              @JsonProperty(value = "requires", required = true)
                              List<Requirement> requirements) {
        _valueSource = amount;
        _filterBlueprint = filterBlueprint;
        _requirements = requirements;
    }

    public Modifier getModifier(ActionContext actionContext) {
        final Evaluator evaluator = _valueSource.getEvaluator(actionContext);
        return new StrengthModifier(actionContext, _filterBlueprint.getFilterable(actionContext),
                new RequirementCondition(_requirements, actionContext), evaluator);
    }

}