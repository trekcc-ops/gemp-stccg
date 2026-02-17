package com.gempukku.stccg.requirement;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Objects;

public class CardInPlayRequirement implements Requirement {

    private final FilterBlueprint _filterBlueprint;
    private final int _countAtLeast;

    @JsonCreator
    private CardInPlayRequirement(
            @JsonProperty("filter") @JsonAlias("cardFilter") FilterBlueprint filterBlueprint,
            @JsonProperty("countAtLeast") Integer countAtLeast
    ) {
        _filterBlueprint = filterBlueprint;
        _countAtLeast = Objects.requireNonNullElse(countAtLeast, 1);
    }

    public boolean accepts(ActionContext actionContext, DefaultGame cardGame) {
        CardFilter filter = _filterBlueprint.getFilterable(cardGame, actionContext);
        return Filters.filterCardsInPlay(cardGame, filter).size() >= _countAtLeast;
    }

}