package com.gempukku.stccg.actions;

import com.gempukku.stccg.actions.choose.SelectCardsAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

import java.util.Collection;
import java.util.LinkedList;

public class ActionCardResolver {

    private Collection<PhysicalCard> _cards;
    private SelectCardsAction _selectAction;
    private Filter _cardFilter;

    public ActionCardResolver(PhysicalCard card) {
        _cards = new LinkedList<>();
        _cards.add(card);
    }

    public ActionCardResolver(Collection<PhysicalCard> cards) {
        _cards = new LinkedList<>(cards);
    }

    public ActionCardResolver(SelectCardsAction selectAction) {
        _selectAction = selectAction;
    }

    public ActionCardResolver(Filter cardFilter) {
        _cardFilter = cardFilter;
    }

    public Collection<PhysicalCard> getCards(DefaultGame cardGame) throws InvalidGameLogicException {
        if (_cards != null) {
            return _cards;
        } else if (_selectAction != null && _selectAction.wasCarriedOut()) {
            return _selectAction.getSelectedCards();
        } else if (_cardFilter != null) {
            return Filters.filter(cardGame, _cardFilter);
        } else {
            throw new InvalidGameLogicException("Unable to identify cards from ActionCardResolver");
        }
    }

}