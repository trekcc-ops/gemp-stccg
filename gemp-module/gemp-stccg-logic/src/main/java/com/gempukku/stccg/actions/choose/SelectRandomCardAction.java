package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.decisions.CardsSelectionDecision;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.google.common.collect.Iterables;

import java.util.Collection;
import java.util.LinkedList;

public class SelectRandomCardAction extends ActionyAction implements SelectCardAction {

    private Collection<? extends PhysicalCard> _selectableCards;
    private final PhysicalCard _actionSource;
    private Filter _cardFilter;
    private PhysicalCard _selectedCard;

    public SelectRandomCardAction(PhysicalCard cardSource, Player selectingPlayer, String choiceText,
                                   Filter cardFilter) {
        super(selectingPlayer, choiceText, ActionType.SELECT_CARD);
        _cardFilter = cardFilter;
        _actionSource = cardSource;
    }

    public SelectRandomCardAction(PhysicalCard cardSource, Player selectingPlayer, String choiceText,
                                   Collection<? extends PhysicalCard> cards) {
        super(selectingPlayer, choiceText, ActionType.SELECT_CARD);
        _selectableCards = cards;
        _actionSource = cardSource;
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
        if (_selectableCards.isEmpty()) {
            throw new InvalidGameLogicException("Could not select a random card from an empty list");
        } else {
            _selectedCard = TextUtils.getRandomItemFromList(_selectableCards);
        }
        return getNextAction();
    }

    @Override
    public boolean wasCarriedOut() {
        return _wasCarriedOut;
    }

    public PhysicalCard getPerformingCard() { return _actionSource; }

    public PhysicalCard getCardForActionSelection() { return _actionSource; }

    public PhysicalCard getSelectedCard() {
        return _selectedCard;
    }

}