package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.scorepoints.ScorePointsAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.evaluator.ValueSource;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.List;

public class ScorePointsSubActionBlueprint implements SubActionBlueprint {

    private final ValueSource _points;

    private ScorePointsSubActionBlueprint(
            @JsonProperty(value = "points")
            ValueSource points
) {
        _points = points;
    }

    @Override
    public List<Action> createActions(DefaultGame cardGame, ActionWithSubActions action, ActionContext context)
            throws InvalidGameLogicException, InvalidCardDefinitionException, PlayerNotFoundException {
        int pointValue = (int) _points.evaluateExpression(cardGame, context);
        Action pointsAction =
                new ScorePointsAction(cardGame, context.card(), context.getPerformingPlayerId(), pointValue);
        return List.of(pointsAction);
    }

}