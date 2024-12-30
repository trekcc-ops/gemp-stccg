package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.CardsSelectionDecision;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;

import java.util.Collection;
import java.util.LinkedList;

/**
 * An effect that causes the specified player to choose cards on the table.
 */
public class SelectVisibleCardsAction extends ActionyAction implements SelectCardsAction {
    private Collection<? extends PhysicalCard> _selectableCards;
    private Collection<PhysicalCard> _selectedCards = new LinkedList<>();
    private final int _minimum;
    private boolean _selectableCardsIdentified = false;
    private Filter _selectionFilter;
    private Integer _maximum;
    private ActionContext _actionContext;
    private String _memory;

    public SelectVisibleCardsAction(Player selectingPlayer, String choiceText,
                                    Collection<? extends PhysicalCard> cards, int minimum) {
        super(selectingPlayer, choiceText, ActionType.SELECT_CARD);
        _selectableCardsIdentified = true;
        _selectableCards = new LinkedList<>(cards);
        _minimum = minimum;
    }

    public SelectVisibleCardsAction(Player selectingPlayer, String choiceText,
                                    Filter selectionFilter, int minimum, int maximum) {
        super(selectingPlayer, choiceText, ActionType.SELECT_CARD);
        _selectionFilter = selectionFilter;
        _minimum = minimum;
        _maximum = maximum;
    }

    public SelectVisibleCardsAction(PhysicalCard performingCard, Player selectingPlayer, String choiceText,
                                    Filter selectionFilter, int minimum, int maximum, ActionContext context,
                                    String memory) {
        super(selectingPlayer, choiceText, ActionType.SELECT_CARD);
        _selectionFilter = selectionFilter;
        _minimum = minimum;
        _maximum = maximum;
        _actionContext = context;
        _memory = memory;
    }


    public boolean requirementsAreMet(DefaultGame game) {
        if (!_selectableCardsIdentified) {
            if (_selectionFilter != null) {
                _selectableCards = Filters.filter(game.getGameState().getAllCardsInGame(), _selectionFilter);
            } else {
                return false;
            }
        }
        return _selectableCards.size() >= _minimum;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        if (!_selectableCardsIdentified) {
            if (_selectionFilter != null) {
                _selectableCards = Filters.filter(cardGame.getGameState().getAllCardsInGame(), _selectionFilter);
            } else {
                throw new InvalidGameLogicException("Unable to select cards. No valid selection filter found.");
            }
        }

        if (_maximum == null) {
            _maximum = _selectableCards.size();
        }

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
                            if (_actionContext != null) {
                                _actionContext.setCardMemory(_memory, _selectedCards);
                            }
                        }
                    });
        }

        return getNextAction();
    }

    @Override
    public boolean wasCarriedOut() {
        return _wasCarriedOut;
    }

    public Collection<PhysicalCard> getSelectedCards() { return _selectedCards; }

}