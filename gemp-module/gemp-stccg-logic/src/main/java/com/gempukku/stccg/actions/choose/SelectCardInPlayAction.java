package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.CardsSelectionDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;
import com.google.common.collect.Iterables;

import java.util.Collection;

/**
 * An effect that causes the specified player to choose a card on the table.
 */
public class SelectCardInPlayAction extends ActionyAction {
    private final Collection<? extends PhysicalCard> _selectableCards;
    private PhysicalCard _selectedCard;
    private final PhysicalCard _actionSource;

    public SelectCardInPlayAction(Action action, Player selectingPlayer, String choiceText,
                                  Collection<? extends PhysicalCard> cards) {
        super(selectingPlayer, choiceText, ActionType.SELECT_CARD);
        _selectableCards = cards;
        _actionSource = action.getActionSource();
    }

    public boolean requirementsAreMet(DefaultGame game) {
        return !_selectableCards.isEmpty();
    }

    @Override
    public Action nextAction(DefaultGame cardGame) {
        if (_selectableCards.size() == 1) {
            _selectedCard = Iterables.getOnlyElement(_selectableCards);
            _wasCarriedOut = true;
        } else {
            cardGame.getUserFeedback().sendAwaitingDecision(
                    new CardsSelectionDecision(cardGame.getPlayer(_performingPlayerId), _text, _selectableCards,
                            1, 1) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            _selectedCard = getSelectedCardByResponse(result);
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

    public PhysicalCard getSelectedCard() { return _selectedCard; }

}