package com.gempukku.stccg.actions.scorepoints;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.Objects;

public class ScorePointsAction extends ActionWithSubActions {

    @JsonProperty("performingCardId")
    @JsonIdentityReference(alwaysAsId=true)
    private final PhysicalCard _performingCard;

    @JsonProperty("pointsScored")
    private final int _points;

    private final boolean _pointsAreBonus;

    public ScorePointsAction(DefaultGame cardGame, PhysicalCard source, String scoringPlayerName,
                             int points, GameTextContext context, boolean pointsAreBonus) {
        super(cardGame, scoringPlayerName, ActionType.SCORE_POINTS, context);
        _performingCard = Objects.requireNonNull(source);
        _points = points;
        _pointsAreBonus = pointsAreBonus;
    }

    public ScorePointsAction(DefaultGame cardGame, PhysicalCard source, Player scoringPlayer, int points,
                             boolean pointsAreBonus)
            throws InvalidGameLogicException {
        super(cardGame, scoringPlayer.getPlayerId(), ActionType.SCORE_POINTS,
                new GameTextContext(source, scoringPlayer.getPlayerId()));
        try {
            _performingCard = Objects.requireNonNull(source);
        } catch(NullPointerException npe) {
            throw new InvalidGameLogicException(npe.getMessage());
        }
        _points = points;
        _pointsAreBonus = pointsAreBonus;
    }


    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    public void processEffect(DefaultGame cardGame) {
        try {
            Player performingPlayer = cardGame.getPlayer(_performingPlayerId);
            performingPlayer.scorePoints(_points, _pointsAreBonus);
            saveResult(new ScorePointsActionResult(cardGame, this, _performingCard, _points), cardGame);
            setAsSuccessful();
        } catch(PlayerNotFoundException exp) {
            cardGame.sendErrorMessage(exp);
            setAsFailed();
        }
    }

    @Override
    public PhysicalCard getPerformingCard() {
        return _performingCard;
    }
}