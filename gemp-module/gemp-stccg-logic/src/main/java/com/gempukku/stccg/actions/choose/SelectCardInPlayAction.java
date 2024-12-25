package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
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
import com.google.common.collect.Iterables;

import java.util.Collection;
import java.util.Collections;

/**
 * An effect that causes the specified player to choose a card on the table.
 */
public class SelectCardInPlayAction extends ActionyAction implements SelectCardsAction {
    private Collection<? extends PhysicalCard> _selectableCards;
    private PhysicalCard _selectedCard;
    private final PhysicalCard _actionSource;
    private final AwaitingDecisionType _decisionType;
    private Filter _cardFilter;

    public SelectCardInPlayAction(Action parentAction, Player selectingPlayer, String choiceText,
                                  Collection<? extends PhysicalCard> cards) {
        super(selectingPlayer, choiceText, ActionType.SELECT_CARD);
        _selectableCards = cards;
        _actionSource = parentAction.getPerformingCard();
        _decisionType = AwaitingDecisionType.CARD_SELECTION;
    }

    public SelectCardInPlayAction(PhysicalCard cardSource, Player selectingPlayer, String choiceText,
                                  Filter cardFilter, AwaitingDecisionType decisionType) {
        super(selectingPlayer, choiceText, ActionType.SELECT_CARD);
        _cardFilter = cardFilter;
        _actionSource = cardSource;
        _decisionType = decisionType;
    }

    public SelectCardInPlayAction(PhysicalCard cardSource, Player selectingPlayer, String choiceText,
                                  Collection<? extends PhysicalCard> cards, AwaitingDecisionType decisionType) {
        this(cardSource, selectingPlayer, choiceText, cards, false, decisionType);
    }


    public SelectCardInPlayAction(PhysicalCard cardSource, Player selectingPlayer, String choiceText,
                                  Collection<? extends PhysicalCard> cards, boolean randomSelection) {
        this(cardSource, selectingPlayer, choiceText, cards, randomSelection, AwaitingDecisionType.CARD_SELECTION);
    }

    public SelectCardInPlayAction(PhysicalCard cardSource, Player selectingPlayer, String choiceText,
                                  Collection<? extends PhysicalCard> cards, boolean randomSelection,
                                  AwaitingDecisionType decisionType) {
        super(selectingPlayer, choiceText, ActionType.SELECT_CARD);
        if (randomSelection) {
            _selectableCards = TextUtils.getRandomItemsFromList(cards, 1);
            _selectedCard = Iterables.getOnlyElement(_selectableCards);
        } else {
            _selectableCards = cards;
        }
        _actionSource = cardSource;
        _decisionType = decisionType;
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
            AwaitingDecision decision =
                    switch(_decisionType) {
                        case CARD_SELECTION -> new CardsSelectionDecision(
                                cardGame.getPlayer(_performingPlayerId), _text, _selectableCards,
                                1, 1) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                _selectedCard = getSelectedCardByResponse(result);
                                _wasCarriedOut = true;
                            }
                        };
                        case ARBITRARY_CARDS -> new ArbitraryCardsSelectionDecision(
                                cardGame.getPlayer(_performingPlayerId), _text, _selectableCards,
                                1, 1) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                _selectedCard = Iterables.getOnlyElement(getSelectedCardsByResponse(result));
                                _wasCarriedOut = true;
                            }
                        };
                        default -> throw new InvalidGameLogicException(
                                "Tried to process a SelectCardInPlayAction with an invalid decision type");
                    };
            cardGame.getUserFeedback().sendAwaitingDecision(decision);
        }

        return getNextAction();
    }

    @Override
    public boolean wasCarriedOut() {
        return _wasCarriedOut;
    }

    public PhysicalCard getPerformingCard() { return _actionSource; }

    public PhysicalCard getCardForActionSelection() { return _actionSource; }

    public PhysicalCard getSelectedCard() { return _selectedCard; }

    @Override
    public Collection<PhysicalCard> getSelectedCards() {
        return Collections.singleton(_selectedCard);
    }
}