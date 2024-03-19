package com.gempukku.stccg.actions.choose;


import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ReturnCardsToHandEffect;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;

import java.util.Collection;

public class ChooseAndReturnCardsToHandEffect extends ChooseActiveCardsEffect {
    private final Action _action;

    public ChooseAndReturnCardsToHandEffect(Action action, String playerId, int minimum, int maximum, Filterable... filters) {
        super(action.getActionSource(), playerId, "Choose cards to return to hand", minimum, maximum, filters);
        _action = action;
    }

    @Override
    protected Filter getExtraFilterForPlaying() {
        return (game1, physicalCard) -> (_action.getActionSource() == null || game1.getModifiersQuerying().canBeReturnedToHand(physicalCard, _action.getActionSource()));
    }

    @Override
    protected void cardsSelected(Collection<PhysicalCard> cards) {
        SubAction subAction = _action.createSubAction();
        subAction.appendEffect(new ReturnCardsToHandEffect(_game, _action.getActionSource(), Filters.in(cards)));
        _game.getActionsEnvironment().addActionToStack(subAction);
    }
}
