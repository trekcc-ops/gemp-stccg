package com.gempukku.stccg.modifiers.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.attributes.CunningModifier;
import com.gempukku.stccg.requirement.Condition;
import com.gempukku.stccg.requirement.Requirement;

import java.util.ArrayList;
import java.util.List;

public class AddRangeModifierBlueprint implements ModifierBlueprint {

    private final FilterBlueprint _modifiedCardFilterBlueprint;
    private final List<Requirement> _requirements = new ArrayList<>();
    private final int _amount;

    AddRangeModifierBlueprint(@JsonProperty(value = "modifiedCards", required = true)
                              FilterBlueprint modifiedCardFilterBlueprint,
                              @JsonProperty(value = "amount", required = true)
                             int amount,
                              @JsonProperty(value = "ifCondition")
                                Requirement ifRequirement) {
        _amount = amount;
        _modifiedCardFilterBlueprint = modifiedCardFilterBlueprint;
        if (ifRequirement != null) {
            _requirements.add(ifRequirement);
        }
    }

    public Modifier createModifier(DefaultGame cardGame, PhysicalCard thisCard, ActionContext actionContext) {
        Filterable affectFilter = _modifiedCardFilterBlueprint.getFilterable(cardGame, actionContext);
        Condition ifCondition = convertRequirementListToCondition(_requirements, actionContext, thisCard, cardGame);
        return new CunningModifier(thisCard, affectFilter, ifCondition, _amount);
    }


}