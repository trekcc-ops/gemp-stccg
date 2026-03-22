package com.gempukku.stccg.modifiers.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.attributes.RangeModifier;
import com.gempukku.stccg.requirement.Condition;
import com.gempukku.stccg.requirement.Requirement;

import java.util.ArrayList;
import java.util.List;

public class AddRangeModifierBlueprint implements ModifierBlueprint {

    private final FilterBlueprint _modifiedCardFilterBlueprint;
    private final List<Requirement> _requirements = new ArrayList<>();
    private final int _amount;
    private final boolean _isCumulative;

    AddRangeModifierBlueprint(@JsonProperty(value = "modifiedCards", required = true)
                              FilterBlueprint modifiedCardFilterBlueprint,
                              @JsonProperty(value = "amount", required = true)
                             int amount,
                              @JsonProperty(value = "ifCondition")
                                Requirement ifRequirement,
                              @JsonProperty(value = "cumulative")
                                 boolean isCumulative) {
        _amount = amount;
        _modifiedCardFilterBlueprint = modifiedCardFilterBlueprint;
        if (ifRequirement != null)
            _requirements.add(ifRequirement);
        _isCumulative = isCumulative;
    }

    public Modifier createModifier(DefaultGame cardGame, PhysicalCard thisCard, GameTextContext actionContext) {
        CardFilter affectFilter = _modifiedCardFilterBlueprint.getFilterable(cardGame, actionContext);
        Condition ifCondition = convertRequirementListToCondition(_requirements, actionContext, thisCard, cardGame);
        return new RangeModifier(thisCard, affectFilter, ifCondition, _amount, _isCumulative);
    }

}