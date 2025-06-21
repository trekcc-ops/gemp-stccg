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

    public ActionCardResolver getTargetResolver(ActionContext context) {
        List<Filterable> selectableCardFilter = new ArrayList<>();
        DefaultGame cardGame = context.getGame();
        for (FilterBlueprint filterBlueprint : _filterBlueprints) {
            selectableCardFilter.add(filterBlueprint.getFilterable(context));
        }
        Collection<PhysicalCard> selectableCards = Filters.filter(cardGame, selectableCardFilter);
        SelectCardsAction selectAction;
        if (_randomSelection) {
            selectAction = new SelectRandomCardAction(
                    context.getGame(), context.getPerformingPlayer(),
                    selectableCards);
        } else {
            selectAction = new SelectCardsFromDialogAction(
                    context.getGame(), context.getPerformingPlayer(), "Select a card",
                    Filters.in(selectableCards));
        }
        return new SelectCardsResolver(selectAction);
    }

    public void addFilter(FilterBlueprint... filterBlueprints) {
        _filterBlueprints.addAll(List.of(filterBlueprints));
    }

}