package com.gempukku.stccg.modifiers.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.FilterFactory;
import com.gempukku.stccg.cards.blueprints.FilterableSource;
import com.gempukku.stccg.cards.blueprints.requirement.Requirement;
import com.gempukku.stccg.common.filterable.CardIcon;
import com.gempukku.stccg.condition.RequirementCondition;
import com.gempukku.stccg.modifiers.GainIconModifier;
import com.gempukku.stccg.modifiers.Modifier;

import java.util.List;

public class GainIconModifierBlueprint implements ModifierBlueprint {

    private final CardIcon _icon;
    private final FilterableSource _filterableSource;
    private final List<Requirement> _requirements;

    GainIconModifierBlueprint(@JsonProperty(value = "icon", required = true)
                              CardIcon icon,
                              @JsonProperty(value = "filter", required = true)
                              String filterText,
                              @JsonProperty(value = "requires")
                              JsonNode requirementNode) throws InvalidCardDefinitionException {
        _icon = icon;
        _filterableSource = new FilterFactory().parseSTCCGFilter(filterText);
        _requirements = BlueprintUtils.getRequirementsFromRequirementNode(requirementNode);
    }

    public Modifier getModifier(ActionContext actionContext) {
        RequirementCondition requirement = new RequirementCondition(_requirements, actionContext);
        return new GainIconModifier(actionContext, _filterableSource.getFilterable(actionContext), requirement, _icon);
    }

}