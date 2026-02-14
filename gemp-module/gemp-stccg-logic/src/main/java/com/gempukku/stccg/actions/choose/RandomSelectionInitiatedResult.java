package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;

public class RandomSelectionInitiatedResult extends ActionResult {

    private final Collection<? extends PhysicalCard> _selectableCards;

    public RandomSelectionInitiatedResult(Action action, Collection<? extends PhysicalCard> selectableCards) {
        super(Type.RANDOM_SELECTION_INITIATED, action.getPerformingPlayerId(), action);
        _selectableCards = selectableCards;
    }

    public boolean includesCardMatchingFilter(DefaultGame cardGame, CardFilter cardFilter) {
        return !Filters.filter(_selectableCards, cardGame, cardFilter).isEmpty();
    }

}