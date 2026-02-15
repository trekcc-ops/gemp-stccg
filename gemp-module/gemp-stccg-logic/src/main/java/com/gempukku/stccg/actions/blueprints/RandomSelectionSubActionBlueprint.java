
package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.choose.SelectRandomCardAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.requirement.Requirement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RandomSelectionSubActionBlueprint implements SubActionBlueprint {

    private final FilterBlueprint _filterBlueprint;
    private final Requirement _requirement;
    private final String _saveToMemoryId;

    public RandomSelectionSubActionBlueprint(
            @JsonProperty("filter") FilterBlueprint filterBlueprint,
            @JsonProperty("requires") Requirement requirementToInitiateSelection,
            @JsonProperty("saveToMemoryId") String saveToMemoryId) {
        _filterBlueprint = filterBlueprint;
        _requirement = requirementToInitiateSelection;
        _saveToMemoryId = Objects.requireNonNullElse(saveToMemoryId, "temp");
    }

    @Override
    public List<Action> createActions(DefaultGame cardGame, ActionWithSubActions action, ActionContext context) {
        List<Action> result = new ArrayList<>();
        if (_requirement.accepts(context, cardGame)) {
            CardFilter filter = _filterBlueprint.getFilterable(cardGame, context);
            String performingPlayer = cardGame.getOpponent(context.getPerformingPlayerId());
            Action selectAction = new SelectRandomCardAction(cardGame, performingPlayer, filter, context, _saveToMemoryId);
            result.add(selectAction);
        }
        return result;
    }

}