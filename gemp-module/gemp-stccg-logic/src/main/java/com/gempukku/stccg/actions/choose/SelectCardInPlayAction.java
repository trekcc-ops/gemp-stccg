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
 * An effect that causes the specified player to choose cards on the table.
 * <p>
 * Note: The choosing of cards provided by this effect does not involve persisting the cards selected or any targeting
 * reasons. This is just choosing cards, and calling the cardsSelected method with the card chosen.
 */
public abstract class SelectCardInPlayAction extends ActionyAction {
    private final String _choiceText;
    private final Collection<PhysicalCard> _selectableCards;
    private PhysicalCard _selectedCard;
    private final PhysicalCard _actionSource;

    public SelectCardInPlayAction(Action action, Player selectingPlayer, String choiceText, Collection<PhysicalCard> cards) {
        super(selectingPlayer, ActionType.SELECT_CARD);
        _choiceText = choiceText;
        _selectableCards = cards;
        _actionSource = action.getActionSource();
    }

    public boolean requirementsAreMet(DefaultGame game) {
        return !_selectableCards.isEmpty();
    }

    @Override
    public Action nextAction(DefaultGame cardGame) {
        if (_selectableCards.size() == 1) {
            cardSelected(Iterables.getOnlyElement(_selectableCards));
        } else {
            cardGame.getUserFeedback().sendAwaitingDecision(
                    new CardsSelectionDecision(cardGame.getPlayer(_performingPlayerId), _choiceText, _selectableCards,
                            1, 1) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            _selectedCard = getSelectedCardByResponse(result);
                            cardSelected(_selectedCard);
                            _wasCarriedOut = true;
                        }
                    });
        }

        return getNextAction();
    }

    /**
     * This method is called when the card has been selected.
     * @param selectedCard the selected cards
     */
    protected abstract void cardSelected(PhysicalCard selectedCard);

    @Override
    public boolean wasCarriedOut() {
        return _wasCarriedOut;
    }

    public PhysicalCard getActionSource() { return _actionSource; }

    public PhysicalCard getCardForActionSelection() { return _actionSource; }

}