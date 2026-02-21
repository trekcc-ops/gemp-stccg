package com.gempukku.stccg.requirement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.game.DefaultGame;

public class ThisCardIsOnPlanetRequirement implements Requirement {

    @JsonProperty("planetFilter")
    FilterBlueprint _planetFilter;

    @JsonCreator
    public ThisCardIsOnPlanetRequirement(@JsonProperty("planetFilter") FilterBlueprint planetFilter) {
        _planetFilter = planetFilter;
    }
    @Override
    public boolean accepts(GameTextContext context, DefaultGame cardGame) {
        CardFilter planetFilter = _planetFilter.getFilterable(cardGame, context);
        return context.card().isOnPlanet(cardGame) && planetFilter.accepts(cardGame, context.card());
    }

}