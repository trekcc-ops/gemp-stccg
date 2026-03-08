package com.gempukku.stccg.actions.scorepoints;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.ActionResultType;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class ScorePointsActionResult extends ActionResult {

    @JsonProperty("performingCardId")
    @JsonIdentityReference(alwaysAsId=true)
    private final PhysicalCard _performingCard;

    @JsonProperty("pointsScored")
    private final int _pointsScored;

    public ScorePointsActionResult(DefaultGame cardGame, ScorePointsAction action, PhysicalCard performingCard,
                                   int pointsScored) {
        super(cardGame, ActionResultType.SCORE_POINTS, action.getPerformingPlayerId(), action);
        _performingCard = performingCard;
        _pointsScored = pointsScored;
    }

}