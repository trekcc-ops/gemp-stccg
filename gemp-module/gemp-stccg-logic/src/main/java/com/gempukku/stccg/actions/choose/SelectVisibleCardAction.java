package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.decisions.CardsSelectionDecision;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.google.common.collect.Iterables;

import java.util.Collection;

/**
 * An effect that causes the specified player to choose a card on the table.
 */
public class SelectVisibleCardAction extends ActionyAction implements SelectCardAction {
    private Collection<? extends PhysicalCard> _selectableCards;
    private Filter _cardFilter;
    private PhysicalCard _selectedCard;

    public SelectVisibleCardAction(Player selectingPlayer, String choiceText,
                                   Collection<? extends PhysicalCard> cards) {
        super(selectingPlayer, choiceText, ActionType.SELECT_CARD);
        _selectableCards = cards;
    }

    public SelectVisibleCardAction(Player selectingPlayer, String choiceText, Filter cardFilter) {
        super(selectingPlayer, choiceText, ActionType.SELECT_CARD);
        _cardFilter = cardFilter;
    }


    public boolean requirementsAreMet(DefaultGame game) {
        if (_selectableCards == null) {
            if (_cardFilter != null) {
                return !Filters.filter(game, _cardFilter).isEmpty();
            } else {
                game.sendMessage("ERROR: Unable to identify eligible cards for SelectCardsInPlayAction");
                return false;
            }
        } else {
            return !_selectableCards.isEmpty();
        }
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        if (_selectableCards == null) {
            _selectableCards = Filters.filter(cardGame, _cardFilter);
        }
        if (_selectableCards.size() == 1) {
            _selectedCard = Iterables.getOnlyElement(_selectableCards);
            _wasCarriedOut = true;
        } else {
            AwaitingDecision decision = new CardsSelectionDecision(
                                cardGame.getPlayer(_performingPlayerId), _text, _selectableCards,
                                1, 1) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                _selectedCard = getSelectedCardByResponse(result);
                                _wasCarriedOut = true;
                            }
                        };
            cardGame.getUserFeedback().sendAwaitingDecision(decision);
        }
        return getNextAction();
    }

    @Override
    public boolean wasCarriedOut() {
        return _wasCarriedOut;
    }

    public PhysicalCard getSelectedCard() {
        return _selectedCard;
    }
}