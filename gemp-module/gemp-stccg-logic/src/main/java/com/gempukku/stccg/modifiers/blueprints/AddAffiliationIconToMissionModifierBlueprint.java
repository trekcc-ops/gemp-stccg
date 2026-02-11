package com.gempukku.stccg.modifiers.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.AddAffiliationIconToMissionModifier;
import com.gempukku.stccg.modifiers.Modifier;

import java.util.List;

public class AddAffiliationIconToMissionModifierBlueprint implements ModifierBlueprint {

    private final FilterBlueprint _modifiedCardFilterBlueprint;
    private final List<Affiliation> _affiliations;
    private final boolean _bothSides;

    AddAffiliationIconToMissionModifierBlueprint(@JsonProperty(value = "modifiedCards", required = true)
                                FilterBlueprint modifiedCardFilterBlueprint,
                                @JsonProperty(value = "affiliation", required = true)
                                List<Affiliation> affiliations,
                                @JsonProperty(value = "bothSides")
                                boolean bothSides) {
        _modifiedCardFilterBlueprint = modifiedCardFilterBlueprint;
        _affiliations = affiliations;
        _bothSides = bothSides;
    }

    public Modifier createModifier(DefaultGame cardGame, PhysicalCard thisCard, ActionContext actionContext) {
        CardFilter affectFilter = _modifiedCardFilterBlueprint.getFilterable(cardGame, actionContext);
        return new AddAffiliationIconToMissionModifier(actionContext, affectFilter,
            _affiliations, _bothSides);
    }

}