package com.gempukku.stccg.actions.targetresolver;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.playcard.EnterPlayActionType;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.evaluator.ValueSource;
import com.gempukku.stccg.filters.AndFilterBlueprint;
import com.gempukku.stccg.filters.CanEnterPlayFilterBlueprint;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.game.DefaultGame;

import java.util.ArrayList;
import java.util.List;

public class ReportCardsResolverBlueprint implements TargetResolverBlueprint {

    private FilterBlueprint _reportableFilterBlueprint;
    private final ValueSource _count;
    private final FilterBlueprint _destinationFilterBlueprint;
    private final boolean _differentCardsOnly;
    private final boolean _specialReporting;

    private ReportCardsResolverBlueprint(@JsonProperty("filter") FilterBlueprint reportableFilterBlueprint,
                                        @JsonProperty("count") ValueSource count,
                                        @JsonProperty("differentCardsOnly") boolean differentCardsOnly,
                                        @JsonProperty("destination") FilterBlueprint destinationFilterBlueprint,
                                        @JsonProperty("specialReporting") boolean specialReporting)
            throws InvalidCardDefinitionException {
        _reportableFilterBlueprint = new AndFilterBlueprint(
                reportableFilterBlueprint,
                new CanEnterPlayFilterBlueprint(EnterPlayActionType.PLAY)
        );
        _count = count;
        _differentCardsOnly = differentCardsOnly;
        _destinationFilterBlueprint = destinationFilterBlueprint;
        _specialReporting = specialReporting;
        if (_specialReporting && _destinationFilterBlueprint == null) {
            throw new InvalidCardDefinitionException("Cannot create an action blueprint to report a card using special" +
                    " reporting if no destination is specified");
        }
    }
    @Override
    public ActionCardResolver getTargetResolver(DefaultGame cardGame, GameTextContext context) {
        CardFilter reportableFilter = _reportableFilterBlueprint.getFilterable(cardGame, context);
        CardFilter destinationFilter = _destinationFilterBlueprint.getFilterable(cardGame, context);
        int minCount;
        int maxCount;
        minCount = _count.getMinimum(cardGame, context);
        maxCount = _count.getMaximum(cardGame, context);
        return new ReportMultipleCardsResolver(reportableFilter, destinationFilter, minCount, maxCount,
                _differentCardsOnly, _specialReporting, cardGame, context.yourName());
    }

    @Override
    public void addFilter(FilterBlueprint... filterBlueprint) {

    }

    public void addReportableFilter(FilterBlueprint... filterBlueprints) {
        List<FilterBlueprint> newFilters = new ArrayList<>();
        newFilters.add(_reportableFilterBlueprint);
        newFilters.addAll(List.of(filterBlueprints));
        _reportableFilterBlueprint = new AndFilterBlueprint(newFilters);
    }

    @Override
    public boolean canBeResolved(DefaultGame cardGame, GameTextContext context) {
        return !getTargetResolver(cardGame, context).cannotBeResolved(cardGame);
    }
}