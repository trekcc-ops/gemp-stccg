package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionCardResolver;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.decisions.CardsSelectionDecision;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.google.common.collect.Iterables;

import java.util.Collection;

/**
 * An effect that causes the specified player to choose a card on the table.
 */
public class SelectVisibleCardAction extends ActionyAction implements SelectCardAction {
    private final ActionCardResolver _selectableCards;
    private PhysicalCard _selectedCard;

    public SelectVisibleCardAction(Player selectingPlayer, String choiceText,
                                   Collection<? extends PhysicalCard> cards) {
        super(selectingPlayer, choiceText, ActionType.SELECT_CARD);
        _selectableCards = new ActionCardResolver(cards);
    }

    public SelectVisibleCardAction(Player selectingPlayer, String choiceText, Filter cardFilter) {
        super(selectingPlayer, choiceText, ActionType.SELECT_CARD);
        _selectableCards = new ActionCardResolver(cardFilter);
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
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        _selectableCards.resolve(cardGame);
        Collection<PhysicalCard> selectableCards = _selectableCards.getCards(cardGame);
        if (selectableCards.size() == 1) {
            _selectedCard = Iterables.getOnlyElement(selectableCards);
            _wasCarriedOut = true;
        } else {
            AwaitingDecision decision = new CardsSelectionDecision(
                                cardGame.getPlayer(_performingPlayerId), _text, selectableCards,
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