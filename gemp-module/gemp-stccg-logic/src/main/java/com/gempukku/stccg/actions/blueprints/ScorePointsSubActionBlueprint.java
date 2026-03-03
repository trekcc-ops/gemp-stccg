package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.scorepoints.ScorePointsAction;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.evaluator.SingleValueSource;
import com.gempukku.stccg.game.DefaultGame;

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
    public ScorePointsAction createAction(DefaultGame cardGame, GameTextContext context) {
        int pointValue = _points.evaluateExpression(cardGame, context);
        ScorePointsAction pointsAction =
                new ScorePointsAction(cardGame, context.card(), context.yourName(), pointValue, context);
        if (_discardThisCard) {
            Zone discardToZone = cardGame.getRules().getDiscardZone(true);
            boolean toPointArea = discardToZone == Zone.POINT_AREA;
            pointsAction.appendCost(new DiscardThisCardSubActionBlueprint(toPointArea));
        }
        return pointsAction;
    }

}