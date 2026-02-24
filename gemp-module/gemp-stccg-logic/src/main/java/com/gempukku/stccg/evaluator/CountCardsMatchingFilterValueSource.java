package com.gempukku.stccg.evaluator;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Objects;

public class CountCardsMatchingFilterValueSource implements SingleValueSource {

    private final int _forEach;
    private final FilterBlueprint _filterBlueprint;

    private CountCardsMatchingFilterValueSource(@JsonProperty("filter")FilterBlueprint filterBlueprint,
                                                @JsonProperty("forEach") Integer forEach) {
        _forEach = Objects.requireNonNullElse(forEach, 1);
        _filterBlueprint = filterBlueprint;
    }

    public Evaluator getEvaluator(DefaultGame cardGame, GameTextContext actionContext) {
        CardFilter filter = _filterBlueprint.getFilterable(cardGame, actionContext);
        return new CountCardsMatchingFilterEvaluator(filter, _forEach);
    }

}