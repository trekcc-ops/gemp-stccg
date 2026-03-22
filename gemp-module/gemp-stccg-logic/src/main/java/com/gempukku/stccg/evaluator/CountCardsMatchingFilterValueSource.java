package com.gempukku.stccg.evaluator;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Objects;

public class CountCardsMatchingFilterValueSource implements SingleValueSource {

    private final float _forEach;
    private final FilterBlueprint _filterBlueprint;
    private final boolean _roundUp;

    private CountCardsMatchingFilterValueSource(@JsonProperty("filter")FilterBlueprint filterBlueprint,
                                                @JsonProperty("forEach") Float forEach,
                                                @JsonProperty("roundUp") boolean roundUp) {
        _forEach = Objects.requireNonNullElse(forEach, 1.0f);
        _filterBlueprint = filterBlueprint;
        _roundUp = roundUp;
    }

    public Evaluator getEvaluator(DefaultGame cardGame, GameTextContext actionContext) {
        CardFilter filter = _filterBlueprint.getFilterable(cardGame, actionContext);
        return new CountCardsMatchingFilterEvaluator(filter, _forEach, _roundUp);
    }

}