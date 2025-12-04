package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.CardPerformedAction;
import com.gempukku.stccg.actions.choose.SelectAndInsertAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectAndPerformSubActionBlueprint implements SubActionBlueprint {

    private final List<SubActionBlueprint> _subActions;
    private final List<String> _actionDescriptions;

    public SelectAndPerformSubActionBlueprint(
            @JsonProperty(value = "actionDescriptions")
            List<String> actionDescriptions,
            @JsonProperty(value = "subActions")
           List<SubActionBlueprint> subActions) {
        _subActions = subActions;
        _actionDescriptions = actionDescriptions;
    }

    @Override
    public List<Action> createActions(DefaultGame cardGame, CardPerformedAction action, ActionContext context)
            throws InvalidGameLogicException, InvalidCardDefinitionException, PlayerNotFoundException {

        if (_subActions.size() != _actionDescriptions.size()) {
            throw new InvalidCardDefinitionException(
                    "Could not create SelectAndPerformSubActionBlueprint because lists were not the same size");
        }

        List<Action> actionsToSelect = new ArrayList<>();
        Map<Action, String> messageMap = new HashMap<>();

        for (int i = 0; i < _subActions.size(); i++) {
            SubActionBlueprint blueprint = _subActions.get(i);
            Action subAction = blueprint.createActions(cardGame, action, context).getFirst();
            actionsToSelect.add(subAction);
            messageMap.put(subAction, _actionDescriptions.get(i));
        }

        return List.of(new SelectAndInsertAction(cardGame, action, context.getPerformingPlayerId(), actionsToSelect,
                messageMap));
    }

}