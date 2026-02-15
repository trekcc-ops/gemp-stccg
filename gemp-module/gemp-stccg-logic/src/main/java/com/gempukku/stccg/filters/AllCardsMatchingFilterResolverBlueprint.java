package com.gempukku.stccg.filters;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.targetresolver.ActionCardResolver;
import com.gempukku.stccg.actions.targetresolver.AllCardsMatchingFilterResolver;
import com.gempukku.stccg.actions.targetresolver.TargetResolverBlueprint;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.game.DefaultGame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class AllCardsMatchingFilterResolverBlueprint implements TargetResolverBlueprint {

    private FilterBlueprint _filterBlueprint;

    public AllCardsMatchingFilterResolverBlueprint(@JsonProperty("filter") FilterBlueprint filterBlueprint) {
        _filterBlueprint = filterBlueprint;
    }

    @Override
    public ActionCardResolver getTargetResolver(DefaultGame cardGame, ActionContext context) {
        CardFilter filter = _filterBlueprint.getFilterable(cardGame, context);
        return new AllCardsMatchingFilterResolver(filter);
    }

    @Override
    public void addFilter(FilterBlueprint... filterBlueprint) {
        Collection<FilterBlueprint> filters = new ArrayList<>();
        filters.add(_filterBlueprint);
        filters.addAll(Arrays.asList(filterBlueprint));
        _filterBlueprint = new AndFilterBlueprint(filters);
    }

    @Override
    public boolean canBeResolved(DefaultGame cardGame, ActionContext context) {
        return !getTargetResolver(cardGame, context).cannotBeResolved(cardGame);
    }
}