package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.CardsSelectionDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;

import java.util.Collection;
import java.util.LinkedList;

/**
 * An effect that causes the specified player to choose cards on the table.
 */
public class SelectCardsOnTableAction extends ActionyAction implements SelectCardsAction {
    private final Collection<? extends PhysicalCard> _selectableCards;
    private Collection<PhysicalCard> _selectedCards = new LinkedList<>();
    private final PhysicalCard _actionSource;
    private final int _minimum;
    private final int _maximum;

    public SelectCardsOnTableAction(Action action, Player selectingPlayer, String choiceText,
                                    Collection<? extends PhysicalCard> cards, int minimum) {
        super(selectingPlayer, choiceText, ActionType.SELECT_CARD);
        _selectableCards = cards;
        _actionSource = action.getActionSource();
        _minimum = minimum;
        _maximum = cards.size();
    }

    public boolean requirementsAreMet(DefaultGame game) {
        return _selectableCards.size() >= _minimum;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) {
        if (_selectableCards.size() == _minimum) {
            _selectedCards.addAll(_selectableCards);
            _wasCarriedOut = true;
        } else {
            cardGame.getUserFeedback().sendAwaitingDecision(
                    new CardsSelectionDecision(cardGame.getPlayer(_performingPlayerId), _text, _selectableCards,
                            _minimum, _maximum) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            _selectedCards = getSelectedCardsByResponse(result);
                            _wasCarriedOut = true;
                        }
                    });
        }

        return getNextAction();
    }

    @Override
    public boolean wasCarriedOut() {
        return _wasCarriedOut;
    }

    public PhysicalCard getActionSource() { return _actionSource; }

    public PhysicalCard getCardForActionSelection() { return _actionSource; }

    public Collection<PhysicalCard> getSelectedCards() { return _selectedCards; }

}