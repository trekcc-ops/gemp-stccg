package com.gempukku.stccg.modifiers.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.condition.Condition;
import com.gempukku.stccg.condition.RequirementCondition;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.attributes.CunningModifier;
import com.gempukku.stccg.modifiers.attributes.StrengthModifier;
import com.gempukku.stccg.requirement.Requirement;

import java.util.List;

public class AddStrengthModifierBlueprint implements ModifierBlueprint {

    private final FilterBlueprint _modifiedCardFilterBlueprint;
    private final Requirement _requirement;
    private final int _amount;

    AddStrengthModifierBlueprint(@JsonProperty(value = "modifiedCards", required = true)
                              FilterBlueprint modifiedCardFilterBlueprint,
                                 @JsonProperty(value = "amount", required = true)
                             int amount,
                                 @JsonProperty(value = "ifCondition", required = true) // thisCardPresentWithYourCard
                                Requirement ifRequirement) { // personnel + affiliation(federation)
        _amount = amount;
        _modifiedCardFilterBlueprint = modifiedCardFilterBlueprint;
        _requirement = ifRequirement;
    }

    public Modifier getModifier(ActionContext actionContext) {
        PhysicalCard thisCard = actionContext.getSource();
        Filterable affectFilter = _modifiedCardFilterBlueprint.getFilterable(actionContext);
        Condition ifCondition = new RequirementCondition(List.of(_requirement), actionContext);
        return new StrengthModifier(thisCard, affectFilter, ifCondition, _amount);
    }
}