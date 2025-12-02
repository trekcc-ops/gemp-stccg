package com.gempukku.stccg.modifiers.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardIcon;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.GainIconModifier;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.requirement.RequirementCondition;

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

    public Modifier createModifier(DefaultGame cardGame, PhysicalCard thisCard, ActionContext actionContext) {
        RequirementCondition requirement = new RequirementCondition(_requirements, actionContext);
        return new GainIconModifier(thisCard,
                _filterBlueprint.getFilterable(cardGame, actionContext), requirement, _icon);
    }

}