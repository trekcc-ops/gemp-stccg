package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.CardsSelectionDecision;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.Collection;
import java.util.LinkedList;

/**
 * An effect that causes the specified player to choose cards on the table.
 */
public class SelectVisibleCardsAction extends ActionyAction implements SelectCardsAction {
    private Collection<PhysicalCard> _selectedCards = new LinkedList<>();
    private final ActionCardResolver _selectableCardsResolver;
    private final int _minimum;
    private Integer _maximum;
    private final String _decisionText;

    public SelectVisibleCardsAction(DefaultGame cardGame, String selectingPlayerName, String choiceText,
                                    Collection<? extends PhysicalCard> cards, int minimum) {
        super(cardGame, selectingPlayerName, choiceText, ActionType.SELECT_CARDS);
        _selectableCardsResolver = new FixedCardsResolver(cards);
        _minimum = minimum;
        _decisionText = choiceText;
    }

    public SelectVisibleCardsAction(DefaultGame cardGame, String performingPlayerName, String choiceText,
                                    CardFilter selectionFilter, int minimum, int maximum) {
        super(cardGame, performingPlayerName, choiceText, ActionType.SELECT_CARDS);
        _selectableCardsResolver = new CardFilterResolver(selectionFilter);
        _minimum = minimum;
        _maximum = maximum;
        _decisionText = choiceText;
    }


    public SelectVisibleCardsAction(DefaultGame cardGame, Player selectingPlayer, String choiceText,
                                    CardFilter selectionFilter, int minimum, int maximum) {
        super(cardGame, selectingPlayer, choiceText, ActionType.SELECT_CARDS);
        _selectableCardsResolver = new CardFilterResolver(selectionFilter);
        _minimum = minimum;
        _maximum = maximum;
        _decisionText = choiceText;
    }


    public boolean requirementsAreMet(DefaultGame game) {
        try {
            Collection<PhysicalCard> selectableCards = _selectableCardsResolver.getCards(game);
            return selectableCards.size() >= _minimum;
        } catch(InvalidGameLogicException exp) {
            game.sendErrorMessage(exp);
            return false;
        }
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException {
        _selectableCardsResolver.resolve(cardGame);
        Collection<PhysicalCard> selectableCards = _selectableCardsResolver.getCards(cardGame);

        if (_maximum == null) {
            _maximum = selectableCards.size();
        }

        if (selectableCards.size() == _minimum) {
            _selectedCards.addAll(selectableCards);
            _wasCarriedOut = true;
            setAsSuccessful();
        } else {
            cardGame.getUserFeedback().sendAwaitingDecision(
                    new CardsSelectionDecision(cardGame.getPlayer(_performingPlayerId), _decisionText, selectableCards,
                            _minimum, _maximum, cardGame) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            _selectedCards = getSelectedCardsByResponse(result);
                            _wasCarriedOut = true;
                            setAsSuccessful();
                        }
                    });
            setAsSuccessful();
        }

        return getNextAction();
    }

    @Override
    public boolean wasCarriedOut() {
        return _wasCarriedOut;
    }

    public Collection<PhysicalCard> getSelectedCards() { return _selectedCards; }

    @Override
    public Collection<? extends PhysicalCard> getSelectableCards(DefaultGame cardGame) {
        try {
            return _selectableCardsResolver.getCards(cardGame);
        } catch(InvalidGameLogicException exp) {
            return new LinkedList<>();
        }
    }

    public int getMinimum() { return _minimum; }
    public int getMaximum() { return _maximum; }

}