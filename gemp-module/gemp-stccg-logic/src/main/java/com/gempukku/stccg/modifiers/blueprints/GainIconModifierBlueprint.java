package com.gempukku.stccg.modifiers.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.common.filterable.CardIcon;
import com.gempukku.stccg.condition.RequirementCondition;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.modifiers.GainIconModifier;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.requirement.Requirement;

import java.util.List;

public class GainIconModifierBlueprint implements ModifierBlueprint {

    private final CardIcon _icon;
    private final FilterBlueprint _filterBlueprint;
    private final List<Requirement> _requirements;

    GainIconModifierBlueprint(@JsonProperty(value = "icon", required = true)
                              CardIcon icon,
                              @JsonProperty(value = "filter", required = true)
                              FilterBlueprint filterBlueprint,
                              @JsonProperty(value = "requires")
                              List<Requirement> requirements) {
        _icon = icon;
        _filterBlueprint = filterBlueprint;
        _requirements = requirements;
    }

    public Modifier getModifier(ActionContext actionContext) {
        RequirementCondition requirement = new RequirementCondition(_requirements, actionContext);
        return new GainIconModifier(actionContext, _filterBlueprint.getFilterable(actionContext), requirement, _icon);
    }

}