package com.gempukku.stccg.modifiers.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.ThisCardIncompatibleWithModifier;

public class ThisCardIncompatibleWithModifierBlueprint implements ModifierBlueprint {
    private final FilterBlueprint _incompatibleFilter;

    private ThisCardIncompatibleWithModifierBlueprint(@JsonProperty("cardFilter") FilterBlueprint incompatibleFilter) {
        _incompatibleFilter = incompatibleFilter;
    }
    @Override
    public Modifier createModifier(DefaultGame cardGame, PhysicalCard thisCard, ActionContext actionContext) {
        CardFilter incompatibleCards = _incompatibleFilter.getFilterable(cardGame, actionContext);
        return new ThisCardIncompatibleWithModifier(thisCard, incompatibleCards);
    }
}