package com.gempukku.stccg.filters;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.targetresolver.ActionCardResolver;
import com.gempukku.stccg.actions.targetresolver.AllCardsMatchingFilterResolver;
import com.gempukku.stccg.actions.targetresolver.TargetResolverBlueprint;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.game.DefaultGame;

public class AllCardsMatchingFilterResolverBlueprint implements TargetResolverBlueprint {

    private FilterBlueprint _filterBlueprint;

    public AllCardsMatchingFilterResolverBlueprint(@JsonProperty("filter") FilterBlueprint filterBlueprint) {
        _filterBlueprint = filterBlueprint;
    }

    @Override
    public ActionCardResolver getTargetResolver(DefaultGame cardGame, GameTextContext context) {
        CardFilter filter = _filterBlueprint.getFilterable(cardGame, context);
        return new AllCardsMatchingFilterResolver(filter);
    }

    @Override
    public boolean canBeResolved(DefaultGame cardGame, GameTextContext context) {
        return !getTargetResolver(cardGame, context).cannotBeResolved(cardGame);
    }
}