package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.AwayTeam;
import com.gempukku.stccg.cards.physicalcard.PhysicalShipCard;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class SelectAttemptingUnitAction extends ActionyAction {
    private final List<String> _presentedOptions = new LinkedList<>();
    private final List<AttemptingUnit> _eligibleUnits;
    private AttemptingUnit _selectedResponse;

    public SelectAttemptingUnitAction(Player player, Collection<AttemptingUnit> attemptingUnits)
            throws InvalidGameLogicException {
        super(player, "Choose an Away Team", ActionType.SELECT_AWAY_TEAM);
        _eligibleUnits = new LinkedList<>(attemptingUnits);
        for (AttemptingUnit unit : _eligibleUnits) {
            String decisionText;
            if (unit instanceof AwayTeam team) {
                decisionText = team.concatenateAwayTeam();
            } else if (unit instanceof PhysicalShipCard ship) {
                decisionText = ship.getTitle();
            } else {
                throw new InvalidGameLogicException(
                        "Tried to select an attempting unit from a list with invalid options");
            }
            _presentedOptions.add(decisionText);
        }
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return false;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) {
        if (_presentedOptions.size() == 1) {
            attemptingUnitChosen(_eligibleUnits.getFirst());
        } else {
            Player performingPlayer = cardGame.getPlayer(_performingPlayerId);
            cardGame.getUserFeedback().sendAwaitingDecision(
                    new MultipleChoiceAwaitingDecision(performingPlayer, _text, _presentedOptions, cardGame) {
                        @Override
                        protected void validDecisionMade(int index, String result) {
                            attemptingUnitChosen(_eligibleUnits.get(index));
                        }
                    });
        }
        return getNextAction();
    }

    private void attemptingUnitChosen(AttemptingUnit attemptingUnit) {
        _wasCarriedOut = true;
        _selectedResponse = attemptingUnit;
    }

    public AttemptingUnit getSelection() { return _selectedResponse; }

    public boolean wasCarriedOut() { return _wasCarriedOut; }

}