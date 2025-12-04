package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.Collection;
import java.util.LinkedList;

public class SelectCardsFromDialogAction extends ActionyAction implements SelectCardsAction {
    private final int _minimum;
    private final int _maximum;
    private Collection<PhysicalCard> _selectedCards;
    private ActionContext _actionContext;
    private String _memory;
    private AwaitingDecision _decision;
    private final ActionCardResolver _selectableCardsTarget;
    private final String _decisionText;

    public SelectCardsFromDialogAction(DefaultGame cardGame, Player selectingPlayer, String choiceText,
                                       CardFilter cardFilter) {
        super(cardGame, selectingPlayer, choiceText, ActionType.SELECT_CARDS);
        _decisionText = choiceText;
        _selectableCardsTarget = new CardFilterResolver(cardFilter);
        _minimum = 1;
        _maximum = 1;
    }

    public SelectCardsFromDialogAction(DefaultGame cardGame, String selectingPlayerName, String choiceText,
                                       CardFilter cardFilter) {
        super(cardGame, selectingPlayerName, choiceText, ActionType.SELECT_CARDS);
        _decisionText = choiceText;
        _selectableCardsTarget = new CardFilterResolver(cardFilter);
        _minimum = 1;
        _maximum = 1;
    }

    public SelectCardsFromDialogAction(DefaultGame cardGame, String selectingPlayerName, String choiceText,
                                       CardFilter cardFilter, int count) {
        super(cardGame, selectingPlayerName, choiceText, ActionType.SELECT_CARDS);
        _decisionText = choiceText;
        _selectableCardsTarget = new CardFilterResolver(cardFilter);
        _minimum = count;
        _maximum = count;
    }




    public boolean requirementsAreMet(DefaultGame game) {
        try {
            return _selectableCardsTarget.getCards(game).size() >= _minimum;
        } catch(Exception exp) {
            return true;
        }
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException {
        _selectableCardsTarget.resolve(cardGame);
        Collection<PhysicalCard> selectableCards = _selectableCardsTarget.getCards(cardGame);
        if (selectableCards.size() == _minimum) {
            _selectedCards = new LinkedList<>(selectableCards);
            setAsSuccessful();
            setCardToMemory();
        } else if (_decision == null) {
            _decision = new ArbitraryCardsSelectionDecision(
                                cardGame.getPlayer(_performingPlayerId), _decisionText, selectableCards,
                                _minimum, _maximum, cardGame) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                _selectedCards = getSelectedCardsByResponse(result);
                                setAsSuccessful();
                                setCardToMemory();
                            }
                        };
            cardGame.getUserFeedback().sendAwaitingDecision(_decision);
        }

        return getNextAction();
    }

    @Override
    public boolean wasCarriedOut() {
        return wasCompleted();
    }

    @Override
    public Collection<PhysicalCard> getSelectedCards() {
        return _selectedCards;
    }

    @Override
    public Collection<? extends PhysicalCard> getSelectableCards(DefaultGame cardGame) {
        try {
            return _selectableCardsTarget.getCards(cardGame);
        } catch(InvalidGameLogicException exp) {
            return new LinkedList<>();
        }
    }

    private void setCardToMemory() {
        if (_actionContext != null) {
            _actionContext.setCardMemory(_memory, _selectedCards);
        }
    }

    public int getMinimum() { return _minimum; }
    public int getMaximum() { return _maximum; }
}