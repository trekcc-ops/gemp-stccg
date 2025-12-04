package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.decisions.CardsSelectionDecision;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.google.common.collect.Iterables;

import java.util.Collection;
import java.util.LinkedList;

/**
 * An effect that causes the specified player to choose a card on the table.
 */
public class SelectVisibleCardAction extends ActionyAction implements SelectCardAction {
    private final ActionCardResolver _selectableCards;
    private PhysicalCard _selectedCard;
    private final String _decisionText;

    public SelectVisibleCardAction(DefaultGame cardGame, String selectingPlayerName, String choiceText,
                                   Collection<? extends PhysicalCard> cards) {
        super(cardGame, selectingPlayerName, choiceText, ActionType.SELECT_CARDS);
        _selectableCards = new FixedCardsResolver(cards);
        _decisionText = choiceText;
    }


    public SelectVisibleCardAction(DefaultGame cardGame, Player selectingPlayer, String choiceText,
                                   Collection<? extends PhysicalCard> cards) {
        super(cardGame, selectingPlayer, choiceText, ActionType.SELECT_CARDS);
        _selectableCards = new FixedCardsResolver(cards);
        _decisionText = choiceText;
    }

    public SelectVisibleCardAction(DefaultGame cardGame, String playerName, String choiceText, CardFilter cardFilter) {
        super(cardGame, playerName, choiceText, ActionType.SELECT_CARDS);
        _selectableCards = new CardFilterResolver(cardFilter);
        _decisionText = choiceText;
    }


    public SelectVisibleCardAction(DefaultGame cardGame, Player selectingPlayer, String choiceText, CardFilter cardFilter) {
        super(cardGame, selectingPlayer, choiceText, ActionType.SELECT_CARDS);
        _selectableCards = new CardFilterResolver(cardFilter);
        _decisionText = choiceText;
    }


    public boolean requirementsAreMet(DefaultGame game) {
        try {
            Collection<PhysicalCard> selectableCards = _selectableCards.getCards(game);
            return !selectableCards.isEmpty();
        } catch(InvalidGameLogicException exp) {
            game.sendErrorMessage(exp);
            return false;
        }
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException {
        _selectableCards.resolve(cardGame);
        Collection<PhysicalCard> selectableCards = _selectableCards.getCards(cardGame);
        if (selectableCards.size() == 1) {
            _selectedCard = Iterables.getOnlyElement(selectableCards);
            _wasCarriedOut = true;
            setAsSuccessful();
        } else {
            AwaitingDecision decision = new CardsSelectionDecision(
                                cardGame.getPlayer(_performingPlayerId), _decisionText, selectableCards,
                                1, 1, cardGame) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                _selectedCard = getSelectedCardByResponse(result);
                                _wasCarriedOut = true;
                                setAsSuccessful();
                            }
                        };
            cardGame.getUserFeedback().sendAwaitingDecision(decision);
            setAsSuccessful();
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

    @Override
    public Collection<? extends PhysicalCard> getSelectableCards(DefaultGame cardGame) {
        try {
            return _selectableCards.getCards(cardGame);
        } catch(InvalidGameLogicException exp) {
            return new LinkedList<>();
        }
    }
}