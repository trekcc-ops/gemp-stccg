package com.gempukku.stccg.modifiers.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardIcon;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.GainIconModifier;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.requirement.Condition;
import com.gempukku.stccg.requirement.Requirement;

import java.util.ArrayList;
import java.util.List;

public class GainIconModifierBlueprint implements ModifierBlueprint {

    private final CardIcon _icon;
    private final FilterBlueprint _filterBlueprint;
    private final List<Requirement> _requirements = new ArrayList<>();

    GainIconModifierBlueprint(@JsonProperty(value = "icon", required = true)
                              CardIcon icon,
                              @JsonProperty(value = "filter", required = true)
                              FilterBlueprint filterBlueprint,
                              @JsonProperty(value = "requires")
                              Requirement requirement) {
        _icon = icon;
        _filterBlueprint = filterBlueprint;
        if (requirement != null) {
            _requirements.add(requirement);
        }
    }

    public Modifier createModifier(DefaultGame cardGame, PhysicalCard thisCard, ActionContext actionContext) {
        Condition ifCondition = convertRequirementListToCondition(_requirements, actionContext, thisCard, cardGame);
        return new GainIconModifier(thisCard,
                _filterBlueprint.getFilterable(cardGame, actionContext), ifCondition, _icon);
    }

}