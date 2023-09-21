package com.gempukku.lotro.effects.choose;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.effects.PutCardFromStackedIntoHandEffect;
import com.gempukku.lotro.actions.SubAction;
import com.gempukku.lotro.actions.Action;

import java.util.Collection;

public class ChooseAndPutCardFromStackedToHandEffect extends ChooseStackedCardsEffect {
    private final Action _action;

    public ChooseAndPutCardFromStackedToHandEffect(Action action, String playerId, int minimum, int maximum, Filterable stackedOn, Filterable... stackedCardsFilter) {
        super(playerId, minimum, maximum, stackedOn, Filters.and(stackedCardsFilter));
        _action = action;
    }

    @Override
    protected void cardsChosen(DefaultGame game, Collection<LotroPhysicalCard> stackedCards) {
        if (stackedCards.size() > 0) {
            SubAction subAction = new SubAction(_action);
            for (LotroPhysicalCard card : stackedCards)
                subAction.appendEffect(new PutCardFromStackedIntoHandEffect(card));
            game.getActionsEnvironment().addActionToStack(subAction);
        }
    }
}
