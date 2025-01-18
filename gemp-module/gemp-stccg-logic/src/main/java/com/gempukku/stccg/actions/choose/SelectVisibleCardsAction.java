package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionCardResolver;
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
    private Collection<PhysicalCard> _selectedCards = new LinkedList<>();
    private final ActionCardResolver _selectableCardsResolver;
    private final int _minimum;
    private Integer _maximum;
    private ActionContext _actionContext;
    private String _memory;

    public SelectVisibleCardsAction(Player selectingPlayer, String choiceText,
                                    Collection<? extends PhysicalCard> cards, int minimum) {
        super(selectingPlayer, choiceText, ActionType.SELECT_CARD);
        _selectableCardsResolver = new ActionCardResolver(cards);
        _minimum = minimum;
    }

    public SelectVisibleCardsAction(Player selectingPlayer, String choiceText, Filter selectionFilter, int minimum,
                                    int maximum) {
        super(selectingPlayer, choiceText, ActionType.SELECT_CARD);
        _selectableCardsResolver = new ActionCardResolver(selectionFilter);
        _minimum = minimum;
        _maximum = maximum;
    }

    public SelectVisibleCardsAction(Player selectingPlayer, String choiceText, Filter selectionFilter, int minimum,
                                    int maximum, ActionContext context, String memory) {
        super(selectingPlayer, choiceText, ActionType.SELECT_CARD);
        _selectableCardsResolver = new ActionCardResolver(selectionFilter);
        _minimum = minimum;
        _maximum = maximum;
        _actionContext = context;
        _memory = memory;
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
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        _selectableCardsResolver.resolve(cardGame);
        Collection<PhysicalCard> selectableCards = _selectableCardsResolver.getCards(cardGame);

        if (_maximum == null) {
            _maximum = selectableCards.size();
        }

        if (selectableCards.size() == _minimum) {
            _selectedCards.addAll(selectableCards);
            _wasCarriedOut = true;
        } else {
            cardGame.getUserFeedback().sendAwaitingDecision(
                    new CardsSelectionDecision(cardGame.getPlayer(_performingPlayerId), _text, selectableCards,
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