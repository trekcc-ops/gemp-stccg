package com.gempukku.stccg.actions.targetresolver;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.choose.SelectCardsAction;
import com.gempukku.stccg.actions.choose.SelectCardsFromDialogAction;
import com.gempukku.stccg.actions.choose.SelectRandomCardAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

import java.util.*;

public class SelectCardTargetBlueprint implements TargetResolverBlueprint {

    private final List<FilterBlueprint> _filterBlueprints;
    private final int _count;
    private final boolean _randomSelection;

    public SelectCardTargetBlueprint(@JsonProperty(value = "filter", required = true)
                              FilterBlueprint filterBlueprint,
                              @JsonProperty("count")
                              Integer count,
                              @JsonProperty("random")
                              Boolean randomSelection) throws InvalidCardDefinitionException {
        _filterBlueprints = new LinkedList<>();
        _filterBlueprints.add(filterBlueprint);
        _count = Objects.requireNonNullElse(count, 1);
        _randomSelection = Objects.requireNonNullElse(randomSelection,false);

        if (_count > 1 && _randomSelection) {
            throw new InvalidCardDefinitionException("Have not implemented SelectCardTargetBlueprint for random " +
                    "selection of multiple cards");
        }
    }

    public SelectCardsResolver getTargetResolver(DefaultGame cardGame, ActionContext context) {
        List<Filterable> selectableCardFilter = new ArrayList<>();
        for (FilterBlueprint filterBlueprint : _filterBlueprints) {
            selectableCardFilter.add(filterBlueprint.getFilterable(cardGame, context));
        }
        CardFilter finalFilter = Filters.and(selectableCardFilter);
        SelectCardsAction selectAction;
        if (_randomSelection) {
            selectAction = new SelectRandomCardAction(
                    cardGame, context.getPerformingPlayerId(),
                    finalFilter);
        } else {
            selectAction = new SelectCardsFromDialogAction(
                    cardGame, context.getPerformingPlayerId(), "Select a card",
                    finalFilter, _count);
        }
        return new SelectCardsResolver(selectAction);
    }

    public boolean canBeResolved(DefaultGame cardGame, ActionContext context) {
        Collection<Filterable> filters = new ArrayList<>();
        for (FilterBlueprint filterBlueprint : _filterBlueprints) {
            CardFilter filter = filterBlueprint.getFilterable(cardGame, context);
            filters.add(filter);
        }
        CardFilter completeFilter = Filters.and(filters);
        Collection<PhysicalCard> filteredCards = Filters.filter(cardGame, completeFilter);
        return filteredCards.size() >= _count;
    }

    public void addFilter(FilterBlueprint... filterBlueprints) {
        _filterBlueprints.addAll(List.of(filterBlueprints));
    }

}