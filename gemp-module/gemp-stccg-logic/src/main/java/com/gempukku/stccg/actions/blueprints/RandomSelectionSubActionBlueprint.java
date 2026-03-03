
package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.choose.SelectRandomCardsAction;
import com.gempukku.stccg.cards.DilemmaEncounterGameTextContext;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.evaluator.ConstantValueSource;
import com.gempukku.stccg.evaluator.ValueSource;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.PlayerSource;
import com.gempukku.stccg.player.YourOpponentPlayerSource;
import com.gempukku.stccg.requirement.Requirement;

import java.util.Collection;
import java.util.Objects;

public class RandomSelectionSubActionBlueprint implements SubActionBlueprint {

    private final FilterBlueprint _filterBlueprint;
    private final Requirement _requirement;
    private final String _saveToMemoryId;
    private final PlayerSource _selectingPlayer = new YourOpponentPlayerSource();
    private final ValueSource _count;

    @JsonCreator
    private RandomSelectionSubActionBlueprint(
            @JsonProperty(value = "filter", required = true) FilterBlueprint filterBlueprint,
            @JsonProperty("requires") Requirement requirementToInitiateSelection,
            @JsonProperty(value = "saveToMemoryId", required = true) String saveToMemoryId,
            @JsonProperty("count") ValueSource count) {
        _filterBlueprint = filterBlueprint;
        _requirement = requirementToInitiateSelection;
        _saveToMemoryId = Objects.requireNonNullElse(saveToMemoryId, "temp");
        _count = Objects.requireNonNullElse(count, new ConstantValueSource(1));
    }

    public SelectRandomCardsAction createAction(DefaultGame cardGame, ActionWithSubActions action,
                                                GameTextContext context) {
        if (_requirement == null || _requirement.accepts(context, cardGame)) {
            CardFilter filter = _filterBlueprint.getFilterable(cardGame, context);
            Collection<PhysicalCard> filteredCards = Filters.filter(cardGame, filter);
            String performingPlayer = _selectingPlayer.getPlayerName(cardGame, context);

            int min = (context instanceof DilemmaEncounterGameTextContext) ?
                    Math.min(_count.getMinimum(cardGame, context), filteredCards.size()) :
                    _count.getMinimum(cardGame, context);
            int max = Math.min(_count.getMaximum(cardGame, context), filteredCards.size());
            return new SelectRandomCardsAction(cardGame, performingPlayer, filter, context, _saveToMemoryId, min, max);
        } else {
            return null;
        }
    }

}