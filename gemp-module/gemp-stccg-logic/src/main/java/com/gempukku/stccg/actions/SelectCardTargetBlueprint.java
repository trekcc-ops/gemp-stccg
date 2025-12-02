package com.gempukku.stccg.actions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.choose.SelectCardsAction;
import com.gempukku.stccg.actions.choose.SelectCardsFromDialogAction;
import com.gempukku.stccg.actions.choose.SelectRandomCardAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.filters.InCardListFilter;
import com.gempukku.stccg.game.DefaultGame;

import java.util.*;

public class SelectCardTargetBlueprint implements CardTargetBlueprint {

    private final List<FilterBlueprint> _filterBlueprints;
    private final int _count;
    private final boolean _randomSelection;

    SelectCardTargetBlueprint(@JsonProperty(value = "filter", required = true)
                              FilterBlueprint filterBlueprint,
                              @JsonProperty("count")
                              int count,
                              @JsonProperty("random")
                              Boolean randomSelection) {
        _filterBlueprints = new LinkedList<>();
        _filterBlueprints.add(filterBlueprint);
        _count = count;
        _randomSelection = Objects.requireNonNullElse(randomSelection,false);
    }

    public ActionCardResolver getTargetResolver(DefaultGame cardGame, ActionContext context) {
        List<Filterable> selectableCardFilter = new ArrayList<>();
        for (FilterBlueprint filterBlueprint : _filterBlueprints) {
            selectableCardFilter.add(filterBlueprint.getFilterable(cardGame, context));
        }
        Collection<PhysicalCard> selectableCards = Filters.filter(cardGame, selectableCardFilter);
        SelectCardsAction selectAction;
        if (_randomSelection) {
            selectAction = new SelectRandomCardAction(
                    cardGame, context.getPerformingPlayerId(), "Select a card",
                    selectableCards);
        } else {
            selectAction = new SelectCardsFromDialogAction(
                    cardGame, context.getPerformingPlayerId(), "Select a card",
                    new InCardListFilter(selectableCards));
        }
        return new SelectCardsResolver(selectAction);
    }

    public void addFilter(FilterBlueprint... filterBlueprints) {
        _filterBlueprints.addAll(List.of(filterBlueprints));
    }

}