package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.discard.DiscardCardToPointAreaAction;
import com.gempukku.stccg.actions.discard.DiscardSingleCardAction;
import com.gempukku.stccg.actions.scorepoints.ScorePointsAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.evaluator.SingleValueSource;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.List;

public class ScorePointsSubActionBlueprint implements SubActionBlueprint {

    private final SingleValueSource _points;
    private final boolean _discardThisCard;

    private ScorePointsSubActionBlueprint(
            @JsonProperty(value = "points")
            SingleValueSource points,
            @JsonProperty(value = "discardThisCard")
            boolean discardThisCard
) {
        _points = points;
        _discardThisCard = discardThisCard;
    }

    @Override
    public List<Action> createActions(DefaultGame cardGame, ActionWithSubActions action, ActionContext context)
            throws InvalidGameLogicException, InvalidCardDefinitionException, PlayerNotFoundException {
        int pointValue = _points.evaluateExpression(cardGame, context);
        Action pointsAction =
                new ScorePointsAction(cardGame, context.card(), context.getPerformingPlayerId(), pointValue);
        if (_discardThisCard) {
            Zone discardToZone = cardGame.getRules().getDiscardZone(true);
            if (discardToZone == Zone.DISCARD) {
                pointsAction.appendCost(new DiscardSingleCardAction(cardGame, context.card(),
                        context.getPerformingPlayerId(), context.card()));
            } else if (discardToZone == Zone.POINT_AREA) {
                pointsAction.appendCost(new DiscardCardToPointAreaAction(cardGame, context.card(),
                        context.getPerformingPlayerId(), context.card()));
            }
        }
        return List.of(pointsAction);
    }

}