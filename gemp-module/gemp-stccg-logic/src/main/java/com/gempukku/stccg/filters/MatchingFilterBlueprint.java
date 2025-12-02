package com.gempukku.stccg.filters;

import com.gempukku.stccg.actions.ActionCardResolver;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

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

    public Filterable getFilterable(DefaultGame cardGame) {
        try {
            CardFilter matchingFilter = new MatchingAffiliationFilter(_cardTarget.getCards(cardGame));
            List<Filterable> finalFilterables = new ArrayList<>();
            finalFilterables.add(matchingFilter);
            finalFilterables.addAll(_additionalFilters);
            return Filters.and(finalFilterables);
        } catch(InvalidGameLogicException exp) {
            cardGame.sendErrorMessage(exp);
            return null;
        }
    }

    @Override
    public Filterable getFilterable(DefaultGame cardGame, ActionContext actionContext) {
        return getFilterable(cardGame);
    }
}