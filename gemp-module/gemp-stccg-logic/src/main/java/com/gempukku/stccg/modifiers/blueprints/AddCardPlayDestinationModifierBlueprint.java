package com.gempukku.stccg.modifiers.blueprints;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.AddCardPlayDestinationModifier;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.requirement.Condition;
import com.gempukku.stccg.requirement.Requirement;

public class AddCardPlayDestinationModifierBlueprint implements ModifierBlueprint {

    private final Requirement _ifCondition;
    private final FilterBlueprint _cardsPlayedFilter;
    private final FilterBlueprint _destinationFilter;

    @JsonCreator
    private AddCardPlayDestinationModifierBlueprint(@JsonProperty("ifCondition")Requirement ifCondition,
                                                    @JsonProperty("cardsPlayedFilter")FilterBlueprint cardsPlayedFilter,
                                                    @JsonProperty("destinationFilter") FilterBlueprint destinationFilter) {
        _ifCondition = ifCondition;
        _cardsPlayedFilter = cardsPlayedFilter;
        _destinationFilter = destinationFilter;
    }

    @Override
    public Modifier createModifier(DefaultGame cardGame, PhysicalCard thisCard, GameTextContext actionContext) {
        Condition condition = _ifCondition.getCondition(actionContext, thisCard, cardGame);
        CardFilter cardsPlayedFilter = _cardsPlayedFilter.getFilterable(cardGame, actionContext);
        CardFilter destinationFilter = _destinationFilter.getFilterable(cardGame, actionContext);
        return new AddCardPlayDestinationModifier(actionContext.card(), cardsPlayedFilter, condition, destinationFilter);
    }
}