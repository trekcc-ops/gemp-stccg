package com.gempukku.lotro.effects.choose;

import com.gempukku.lotro.actions.Action;
import com.gempukku.lotro.actions.SubAction;
import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.common.EndOfPile;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.effects.PutCardsFromZoneOnEndOfPileEffect;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.DefaultGame;

import java.util.Collection;

public class ChooseAndPutCardFromStackedOnTopOfDeckEffect extends ChooseStackedCardsEffect {
    private final Action _action;

    public ChooseAndPutCardFromStackedOnTopOfDeckEffect(Action action, String playerId, int minimum, int maximum, Filterable stackedOn, Filterable... stackedCardsFilter) {
        super(playerId, minimum, maximum, stackedOn, Filters.and(stackedCardsFilter));
        _action = action;
    }

    @Override
    protected void cardsChosen(DefaultGame game, Collection<PhysicalCard> stackedCards) {
        if (stackedCards.size() > 0) {
            SubAction subAction = new SubAction(_action);
            for (PhysicalCard card : stackedCards)
                subAction.appendEffect(new PutCardsFromZoneOnEndOfPileEffect(true, Zone.STACKED, Zone.DRAW_DECK, EndOfPile.TOP, card));
            game.getActionsEnvironment().addActionToStack(subAction);
        }
    }
}
