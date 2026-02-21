
package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.choose.SelectRandomCardsAction;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.PlayerSource;
import com.gempukku.stccg.player.YourOpponentPlayerSource;
import com.gempukku.stccg.requirement.Requirement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RandomSelectionSubActionBlueprint implements SubActionBlueprint {

    private final FilterBlueprint _filterBlueprint;
    private final Requirement _requirement;
    private final String _saveToMemoryId;
    private final PlayerSource _selectingPlayer = new YourOpponentPlayerSource();

    public RandomSelectionSubActionBlueprint(
            @JsonProperty("filter") FilterBlueprint filterBlueprint,
            @JsonProperty("requires") Requirement requirementToInitiateSelection,
            @JsonProperty("saveToMemoryId") String saveToMemoryId) {
        _filterBlueprint = filterBlueprint;
        _requirement = requirementToInitiateSelection;
        _saveToMemoryId = Objects.requireNonNullElse(saveToMemoryId, "temp");
    }

    @Override
    public List<Action> createActions(DefaultGame cardGame, ActionWithSubActions action, GameTextContext context) {
        List<Action> result = new ArrayList<>();
        if (_requirement.accepts(context, cardGame)) {
            CardFilter filter = _filterBlueprint.getFilterable(cardGame, context);
            String performingPlayer = _selectingPlayer.getPlayerName(cardGame, context);
            Action selectAction =
                    new SelectRandomCardsAction(cardGame, performingPlayer, filter, context, _saveToMemoryId, 1);
            result.add(selectAction);
        }
        return result;
    }

}