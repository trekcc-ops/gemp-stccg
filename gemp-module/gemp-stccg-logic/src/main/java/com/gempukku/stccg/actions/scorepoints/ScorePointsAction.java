package com.gempukku.stccg.actions.scorepoints;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

import java.util.Objects;

public class ScorePointsAction extends ActionyAction {
    private final PhysicalCard _performingCard;
    private final int _points;
    private enum Progress { wasCarriedOut }
    public ScorePointsAction(DefaultGame game, PhysicalCard source, String scoringPlayer, int points)
            throws InvalidGameLogicException {
        super(game.getPlayer(scoringPlayer), "Score " + points + " points",
                ActionType.SCORE_POINTS, Progress.values());
        try {
            _performingCard = Objects.requireNonNull(source);
        } catch(NullPointerException npe) {
            throw new InvalidGameLogicException(npe.getMessage());
        }
        _points = points;
    }

    @Override
    public String getActionSelectionText(DefaultGame cardGame) {
        return _performingPlayerId + " scored " + _points + " from " + _performingCard.getCardLink();
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        cardGame.getGameState().addToPlayerScore(_performingPlayerId, _points);
        return getNextAction();
    }
}