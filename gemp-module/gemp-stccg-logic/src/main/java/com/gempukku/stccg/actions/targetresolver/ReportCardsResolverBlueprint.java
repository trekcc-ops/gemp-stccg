package com.gempukku.stccg.actions.targetresolver;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.playcard.PlayCardAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.evaluator.ValueSource;
import com.gempukku.stccg.filters.AndFilterBlueprint;
import com.gempukku.stccg.filters.CanEnterPlayFilterBlueprint;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

public class ReportCardsResolverBlueprint implements TargetResolverBlueprint {

    private final FilterBlueprint _reportableFilterBlueprint;
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
                new CanEnterPlayFilterBlueprint(PlayCardAction.EnterPlayActionType.PLAY)
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
    public ActionCardResolver getTargetResolver(DefaultGame cardGame, ActionContext context) {
        CardFilter reportableFilter = _reportableFilterBlueprint.getFilterable(cardGame, context);
        CardFilter destinationFilter = _destinationFilterBlueprint.getFilterable(cardGame, context);
        int minCount;
        int maxCount;
        try {
            minCount = (int) _count.getMinimum(cardGame, context);
            maxCount = (int) _count.getMaximum(cardGame, context);
        } catch(InvalidGameLogicException exp) {
            minCount = 0;
            maxCount = 0;
        }
        return new ReportMultipleCardsResolver(reportableFilter, destinationFilter, minCount, maxCount,
                _differentCardsOnly, _specialReporting, cardGame, context.getPerformingPlayerId());
    }

    @Override
    public void addFilter(FilterBlueprint... filterBlueprint) {

    }

    @Override
    public boolean canBeResolved(DefaultGame cardGame, ActionContext context) {
        return !getTargetResolver(cardGame, context).cannotBeResolved(cardGame);
    }
}