package com.gempukku.stccg.effects.choose;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.Filterable;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.effects.ReturnCardsToHandEffect;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.actions.Action;

import java.util.Collection;

public class ChooseAndReturnCardsToHandEffect extends ChooseActiveCardsEffect {
    private final Action _action;

    public ChooseAndReturnCardsToHandEffect(Action action, String playerId, int minimum, int maximum, Filterable... filters) {
        super(action.getActionSource(), playerId, "Choose cards to return to hand", minimum, maximum, filters);
        _action = action;
    }

    @Override
    protected Filter getExtraFilterForPlaying(DefaultGame game) {
        return (game1, physicalCard) -> (_action.getActionSource() == null || game1.getModifiersQuerying().canBeReturnedToHand(game1, physicalCard, _action.getActionSource()));
    }

    @Override
    protected void cardsSelected(DefaultGame game, Collection<PhysicalCard> cards) {
        SubAction subAction = new SubAction(_action);
        subAction.appendEffect(new ReturnCardsToHandEffect(_action.getActionSource(), Filters.in(cards)));
        game.getActionsEnvironment().addActionToStack(subAction);
    }
}
