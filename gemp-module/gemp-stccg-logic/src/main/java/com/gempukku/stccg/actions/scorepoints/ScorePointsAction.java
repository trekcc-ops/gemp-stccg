package com.gempukku.stccg.actions.scorepoints;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.*;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.Objects;

public class ScorePointsAction extends ActionyAction {

    @JsonProperty("performingCardId")
    @JsonIdentityReference(alwaysAsId=true)
    private final PhysicalCard _performingCard;

    @JsonProperty("pointsScored")
    private final int _points;

    public ScorePointsAction(DefaultGame cardGame, PhysicalCard source, Player scoringPlayer, int points)
            throws InvalidGameLogicException {
        super(cardGame, scoringPlayer, "Score " + points + " points", ActionType.SCORE_POINTS);
        try {
            _performingCard = Objects.requireNonNull(source);
        } catch(NullPointerException npe) {
            throw new InvalidGameLogicException(npe.getMessage());
        }
        _points = points;
    }


    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException {
        Action nextAction = getNextAction();
        if (nextAction == null) {
            processEffect(cardGame);
        }
        return nextAction;
    }

    public void processEffect(DefaultGame cardGame) {
        try {
            Player performingPlayer = cardGame.getPlayer(_performingPlayerId);
            performingPlayer.scorePoints(_points);
            setAsSuccessful();
        } catch(PlayerNotFoundException exp) {
            cardGame.sendErrorMessage(exp);
            setAsFailed();
        }
    }
}