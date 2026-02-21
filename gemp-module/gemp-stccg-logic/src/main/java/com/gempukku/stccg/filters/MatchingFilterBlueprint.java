package com.gempukku.stccg.filters;

import com.gempukku.stccg.actions.targetresolver.ActionCardResolver;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.game.DefaultGame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MatchingFilterBlueprint implements FilterBlueprint {

    private final ActionCardResolver _cardTarget;
    private final List<Filterable> _additionalFilters = new ArrayList<>();

    public MatchingFilterBlueprint(ActionCardResolver cardTarget, Filterable... additionalFilters) {
        _cardTarget = cardTarget;
        _additionalFilters.addAll(Arrays.asList(additionalFilters));

    }

    @Override
    public CardFilter getFilterable(DefaultGame cardGame, GameTextContext actionContext) {
        CardFilter matchingFilter =
                new MatchingAffiliationFilter(_cardTarget.getCards(cardGame), actionContext.yourName());
        List<Filterable> finalFilterables = new ArrayList<>();
        finalFilterables.add(matchingFilter);
        finalFilterables.addAll(_additionalFilters);
        return Filters.and(finalFilterables);
    }
}