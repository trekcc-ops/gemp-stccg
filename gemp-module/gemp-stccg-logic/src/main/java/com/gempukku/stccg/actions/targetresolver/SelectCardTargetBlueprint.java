package com.gempukku.stccg.actions.targetresolver;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.choose.SelectCardsAction;
import com.gempukku.stccg.actions.choose.SelectCardsFromDialogAction;
import com.gempukku.stccg.actions.choose.SelectRandomCardsAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.evaluator.ConstantValueSource;
import com.gempukku.stccg.evaluator.SingleValueSource;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.PlayerSource;
import com.gempukku.stccg.player.YouPlayerSource;
import com.gempukku.stccg.player.YourOpponentPlayerSource;

import java.util.*;

public class SelectCardTargetBlueprint implements TargetResolverBlueprint {

    private final List<FilterBlueprint> _filterBlueprints;
    private final SingleValueSource _count;
    private final boolean _randomSelection;
    private final PlayerSource _selectingPlayer;
    private final String _saveToMemoryId;

    public SelectCardTargetBlueprint(@JsonProperty(value = "filter", required = true)
                              FilterBlueprint filterBlueprint,
                              @JsonProperty("count")
                             SingleValueSource count,
                              @JsonProperty("selectingPlayer") String selectingPlayerText,
                              @JsonProperty("random")
                              Boolean randomSelection,
                             @JsonProperty("saveToMemoryId") String saveToMemoryId) throws InvalidCardDefinitionException {
        _filterBlueprints = new ArrayList<>(List.of(filterBlueprint));
        if (Objects.equals(selectingPlayerText, "opponent")) {
            _selectingPlayer = new YourOpponentPlayerSource();
        } else if (selectingPlayerText == null) {
            _selectingPlayer = new YouPlayerSource();
        } else {
            throw new InvalidCardDefinitionException("Could not process 'selectingPlayer' for SelectCardTargetBlueprint");
        }
        _count = Objects.requireNonNullElse(count, new ConstantValueSource(1));
        _randomSelection = Objects.requireNonNullElse(randomSelection,false);
        _saveToMemoryId = Objects.requireNonNullElse(saveToMemoryId, "temp");
    }

    public SelectCardsResolver getTargetResolver(DefaultGame cardGame, ActionContext context) {
        List<Filterable> selectableCardFilter = new ArrayList<>();
        String selectingPlayerName = _selectingPlayer.getPlayerName(cardGame, context);
        for (FilterBlueprint filterBlueprint : _filterBlueprints) {
            selectableCardFilter.add(filterBlueprint.getFilterable(cardGame, context));
        }
        CardFilter finalFilter = Filters.and(selectableCardFilter);
        SelectCardsAction selectAction;
        int count = _count.evaluateExpression(cardGame, context);
        if (_randomSelection) {
            selectAction = new SelectRandomCardsAction(cardGame, selectingPlayerName, finalFilter, count);
        } else {
            selectAction = new SelectCardsFromDialogAction(cardGame, selectingPlayerName, finalFilter, count);
        }
        return new SelectCardsResolver(selectAction, context, _saveToMemoryId);
    }

    public boolean canBeResolved(DefaultGame cardGame, ActionContext context) {
        Collection<Filterable> filters = new ArrayList<>();
        for (FilterBlueprint filterBlueprint : _filterBlueprints) {
            CardFilter filter = filterBlueprint.getFilterable(cardGame, context);
            filters.add(filter);
        }
        CardFilter completeFilter = Filters.and(filters);
        Collection<PhysicalCard> filteredCards = Filters.filter(cardGame, completeFilter);
        return filteredCards.size() >= _count.evaluateExpression(cardGame, context);
    }

    public void addFilter(FilterBlueprint... filterBlueprints) {
        _filterBlueprints.addAll(List.of(filterBlueprints));
    }

}