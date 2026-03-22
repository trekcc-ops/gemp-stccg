package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.choose.SelectAndInsertAction;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.game.DefaultGame;

import java.util.*;

public class SelectAndPerformSubActionBlueprint implements SubActionBlueprint {

    private final List<SubActionBlueprint> _subActions;
    private final List<String> _actionDescriptions;

    private SelectAndPerformSubActionBlueprint(
            @JsonProperty(value = "actionDescriptions")
            List<String> actionDescriptions,
            @JsonProperty(value = "subActions")
           List<SubActionBlueprint> subActions) throws InvalidCardDefinitionException {
        _subActions = subActions;
        _actionDescriptions = actionDescriptions;
        if (_subActions.size() != _actionDescriptions.size()) {
            throw new InvalidCardDefinitionException(
                    "Could not create SelectAndPerformSubActionBlueprint because lists were not the same size");
        }
    }

    @Override
    public SelectAndInsertAction createAction(DefaultGame cardGame, GameTextContext context) {
        List<Action> actionsToSelect = new ArrayList<>();
        Map<Action, String> messageMap = new HashMap<>();

        for (int i = 0; i < _subActions.size(); i++) {
            SubActionBlueprint blueprint = _subActions.get(i);
            Action subAction = blueprint.createAction(cardGame, context);
            if (subAction != null) {
                actionsToSelect.add(subAction);
                messageMap.put(subAction, _actionDescriptions.get(i));
            }
        }

        if (!messageMap.isEmpty()) {
            return new SelectAndInsertAction(cardGame, context.yourName(), actionsToSelect, messageMap);
        } else {
            return null;
        }
    }

    @Override
    public Collection<ActionBlueprint> getAllTheoreticalSubActions() {
        return new ArrayList<>(_subActions);
    }

}