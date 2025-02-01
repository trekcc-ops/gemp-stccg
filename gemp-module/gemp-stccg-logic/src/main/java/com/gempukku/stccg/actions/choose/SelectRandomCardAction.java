package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;

import java.util.Collection;
import java.util.LinkedList;

public class SelectRandomCardAction extends ActionyAction implements SelectCardAction {

    private final ActionCardResolver _selectableCardTarget;
    private PhysicalCard _selectedCard;

    public SelectRandomCardAction(DefaultGame cardGame, Player selectingPlayer, String choiceText, Filter cardFilter) {
        super(cardGame, selectingPlayer, choiceText, ActionType.SELECT_CARDS);
        _selectableCardTarget = new CardFilterResolver(cardFilter);
    }

    public SelectRandomCardAction(DefaultGame cardGame, Player selectingPlayer, String choiceText,
                                  Collection<? extends PhysicalCard> cards) {
        super(cardGame, selectingPlayer, choiceText, ActionType.SELECT_CARDS);
        _selectableCardTarget = new FixedCardsResolver(cards);
    }


    public boolean requirementsAreMet(DefaultGame game) {
        try {
            return !_selectableCardTarget.getCards(game).isEmpty();
        } catch(InvalidGameLogicException exp) {
            return true;
        }
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        _selectableCardTarget.resolve(cardGame);
        Collection<PhysicalCard> selectableCards = _selectableCardTarget.getCards(cardGame);
        if (selectableCards.isEmpty()) {
            setAsFailed();
            throw new InvalidGameLogicException("Could not select a random card from an empty list");
        } else {
            _selectedCard = TextUtils.getRandomItemFromList(selectableCards);
            if (_selectedCard == null) {
                setAsFailed();
            } else {
                setAsSuccessful();
            }
        }
        return getNextAction();
    }

    @Override
    public boolean wasCarriedOut() {
        return wasCompleted();
    }

    public PhysicalCard getSelectedCard() {
        return _selectedCard;
    }

    @Override
    public Collection<? extends PhysicalCard> getSelectableCards(DefaultGame cardGame) {
        try {
            return _selectableCardTarget.getCards(cardGame);
        } catch(InvalidGameLogicException exp) {
            return new LinkedList<>();
        }
    }
}