package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.AwaitingDecisionType;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.decisions.CardsSelectionDecision;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;

import java.util.Collection;
import java.util.LinkedList;

public class SelectCardsFromDialogAction extends ActionyAction implements SelectCardsAction {
    private Collection<? extends PhysicalCard> _selectableCards;
    private final PhysicalCard _actionSource;
    private Filter _cardFilter;
    private final int _minimum;
    private final int _maximum;
    private Collection<PhysicalCard> _selectedCards;
    private ActionContext _actionContext;
    private String _memory;

    public SelectCardsFromDialogAction(PhysicalCard cardSource, Player selectingPlayer, String choiceText,
                                       Filter cardFilter) {
        super(selectingPlayer, choiceText, ActionType.SELECT_CARD);
        _cardFilter = cardFilter;
        _actionSource = cardSource;
        _minimum = 1;
        _maximum = 1;
    }

    public SelectCardsFromDialogAction(PhysicalCard cardSource, Player selectingPlayer, String choiceText,
                                       Filter cardFilter, int minimum, int maximum,
                                       ActionContext context, String memory) {
        super(selectingPlayer, choiceText, ActionType.SELECT_CARD);
        _cardFilter = cardFilter;
        _actionSource = cardSource;
        _minimum = minimum;
        _maximum = maximum;
        _actionContext = context;
        _memory = memory;
    }


    public SelectCardsFromDialogAction(PhysicalCard cardSource, Player selectingPlayer, String choiceText,
                                       Collection<? extends PhysicalCard> cards) {
        super(selectingPlayer, choiceText, ActionType.SELECT_CARD);
        _selectableCards = cards;
        _actionSource = cardSource;
        _minimum = 1;
        _maximum = 1;
    }


    public boolean requirementsAreMet(DefaultGame game) {
        if (_selectableCards == null) {
            if (_cardFilter != null) {
                return Filters.filter(game, _cardFilter).size() >= _minimum;
            } else {
                game.sendMessage("ERROR: Unable to identify eligible cards for SelectCardsInPlayAction");
                return false;
            }
        } else {
            return _selectableCards.size() >= _minimum;
        }
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        if (_selectableCards == null) {
            _selectableCards = Filters.filter(cardGame, _cardFilter);
        }
        if (_selectableCards.size() == _minimum) {
            _selectedCards = new LinkedList<>(_selectableCards);
            _wasCarriedOut = true;
        } else {
            AwaitingDecision decision = new ArbitraryCardsSelectionDecision(
                                cardGame.getPlayer(_performingPlayerId), _text, _selectableCards,
                                _minimum, _maximum) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                _selectedCards = getSelectedCardsByResponse(result);
                                _wasCarriedOut = true;
                            }
                        };
            cardGame.getUserFeedback().sendAwaitingDecision(decision);
        }

        if (_actionContext != null) {
            _actionContext.setCardMemory(_memory, _selectedCards);
        }

        return getNextAction();
    }

    @Override
    public boolean wasCarriedOut() {
        return _wasCarriedOut;
    }

    public PhysicalCard getPerformingCard() { return _actionSource; }

    @Override
    public Collection<PhysicalCard> getSelectedCards() {
        return _selectedCards;
    }
}