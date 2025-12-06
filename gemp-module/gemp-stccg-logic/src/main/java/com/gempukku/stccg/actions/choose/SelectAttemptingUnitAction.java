package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.AwayTeam;
import com.gempukku.stccg.cards.physicalcard.ShipCard;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class SelectAttemptingUnitAction extends ActionyAction {
    private final List<String> _presentedOptions = new LinkedList<>();
    private final List<AttemptingUnit> _eligibleUnits;
    private AttemptingUnit _selectedResponse;
    private final String _decisionText;

    public SelectAttemptingUnitAction(DefaultGame cardGame, Player player, Collection<AttemptingUnit> attemptingUnits,
                                      String selectionText)
            throws InvalidGameLogicException {
        super(cardGame, player, selectionText, ActionType.SELECT_AWAY_TEAM);
        _decisionText = selectionText;
        _eligibleUnits = new LinkedList<>(attemptingUnits);
        for (AttemptingUnit unit : _eligibleUnits) {
            String decisionText;
            if (unit instanceof AwayTeam team) {
                decisionText = team.concatenateAwayTeam();
            } else if (unit instanceof ShipCard ship) {
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
    public Action nextAction(DefaultGame cardGame) throws PlayerNotFoundException {
        if (_presentedOptions.size() == 1) {
            attemptingUnitChosen(_eligibleUnits.getFirst());
        } else {
            Player performingPlayer = cardGame.getPlayer(_performingPlayerId);
            cardGame.getUserFeedback().sendAwaitingDecision(
                    new MultipleChoiceAwaitingDecision(performingPlayer, _decisionText, _presentedOptions, cardGame) {
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
        setAsSuccessful();
        _selectedResponse = attemptingUnit;
    }

    public AttemptingUnit getSelection() { return _selectedResponse; }

    public boolean wasCarriedOut() { return _wasCarriedOut; }

}