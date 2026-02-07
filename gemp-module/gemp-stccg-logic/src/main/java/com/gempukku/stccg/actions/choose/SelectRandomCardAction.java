package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

import java.util.Collection;

public class SelectRandomCardAction extends ActionyAction implements SelectCardAction {

    private final CardFilter _selectableCardsFilter;
    private PhysicalCard _selectedCard;

    public SelectRandomCardAction(DefaultGame cardGame, String selectingPlayerName, CardFilter cardFilter) {
        super(cardGame, selectingPlayerName, ActionType.SELECT_CARDS);
        _selectableCardsFilter = cardFilter;
    }


    public boolean requirementsAreMet(DefaultGame game) {
        return !getSelectableCards(game).isEmpty();
    }

    @Override
    protected void processEffect(DefaultGame cardGame) {
        try {
            Collection<? extends PhysicalCard> selectableCards = getSelectableCards(cardGame);
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
        } catch(InvalidGameLogicException exp) {
            cardGame.sendErrorMessage(exp);
            setAsFailed();
        }
    }

    public PhysicalCard getSelectedCard() {
        return _selectedCard;
    }

    @Override
    public Collection<? extends PhysicalCard> getSelectableCards(DefaultGame cardGame) {
        return Filters.filter(cardGame, _selectableCardsFilter);
    }
}