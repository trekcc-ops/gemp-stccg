package com.gempukku.stccg.modifiers.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.CardCompatibleWithAllYourCardsModifier;
import com.gempukku.stccg.modifiers.Modifier;

public class CardCompatibleWithAllYourCardsModifierBlueprint implements ModifierBlueprint {

    private final FilterBlueprint _filterBlueprint1;

    private CardCompatibleWithAllYourCardsModifierBlueprint(@JsonProperty("cardFilter") FilterBlueprint filter1) {
        _filterBlueprint1 = filter1;
    }
    @Override
    public Modifier createModifier(DefaultGame cardGame, PhysicalCard thisCard, GameTextContext context) {
        CardFilter filter1 = _filterBlueprint1.getFilterable(cardGame, context);
        return new CardCompatibleWithAllYourCardsModifier(context.card(), context.yourName(), filter1);
    }
}