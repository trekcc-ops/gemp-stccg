package com.gempukku.stccg.modifiers.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.condition.Condition;
import com.gempukku.stccg.condition.RequirementCondition;
import com.gempukku.stccg.condition.TrueCondition;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.attributes.CunningModifier;
import com.gempukku.stccg.requirement.Requirement;

import java.util.List;
import java.util.Objects;

public class AddCunningModifierBlueprint implements ModifierBlueprint {

    private final FilterBlueprint _modifiedCardFilterBlueprint;
    private final Requirement _requirement;
    private final int _amount;

    AddCunningModifierBlueprint(@JsonProperty(value = "modifiedCards", required = true)
                              FilterBlueprint modifiedCardFilterBlueprint,
                                @JsonProperty(value = "amount", required = true)
                             int amount,
                                @JsonProperty(value = "ifCondition", required = true)
                                Requirement ifRequirement) {
        _amount = amount;
        _modifiedCardFilterBlueprint = modifiedCardFilterBlueprint;
        _requirement = ifRequirement;
    }

    public Modifier getModifier(ActionContext actionContext) {
        PhysicalCard thisCard = actionContext.getSource();
        Filterable affectFilter = _modifiedCardFilterBlueprint.getFilterable(actionContext);
        Condition ifCondition = new RequirementCondition(List.of(_requirement), actionContext);
        return new CunningModifier(thisCard, affectFilter, ifCondition, _amount);
    }

}