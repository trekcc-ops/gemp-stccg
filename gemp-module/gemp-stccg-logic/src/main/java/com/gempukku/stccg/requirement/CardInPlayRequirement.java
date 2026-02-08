package com.gempukku.stccg.requirement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

public class CardInPlayRequirement implements Requirement {

    private final FilterBlueprint _filterBlueprint;

    @JsonCreator
    private CardInPlayRequirement(
            @JsonProperty("filter")
            FilterBlueprint filterBlueprint
    ) {
        _filterBlueprint = filterBlueprint;
    }

    @Override
    public boolean accepts(ActionContext actionContext, DefaultGame cardGame) {
        CardFilter filter = _filterBlueprint.getFilterable(cardGame, actionContext);
        return !Filters.filterCardsInPlay(cardGame, filter).isEmpty();
    }

}