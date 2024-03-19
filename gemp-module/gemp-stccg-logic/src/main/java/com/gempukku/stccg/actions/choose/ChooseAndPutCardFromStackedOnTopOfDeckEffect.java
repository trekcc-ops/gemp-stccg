package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.PutCardsFromZoneOnEndOfPileEffect;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.EndOfPile;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;

public class ChooseAndPutCardFromStackedOnTopOfDeckEffect extends ChooseStackedCardsEffect {
    private final Action _action;

    public ChooseAndPutCardFromStackedOnTopOfDeckEffect(DefaultGame game, Action action, String playerId, int minimum, int maximum, Filterable stackedOn, Filterable... stackedCardsFilter) {
        super(game, playerId, minimum, maximum, stackedOn, Filters.and(stackedCardsFilter));
        _action = action;
    }

    @Override
    protected void cardsChosen(DefaultGame game, Collection<PhysicalCard> stackedCards) {
        if (!stackedCards.isEmpty()) {
            SubAction subAction = _action.createSubAction();
            for (PhysicalCard card : stackedCards)
                subAction.appendEffect(new PutCardsFromZoneOnEndOfPileEffect(game,true, Zone.STACKED, Zone.DRAW_DECK, EndOfPile.TOP, card));
            game.getActionsEnvironment().addActionToStack(subAction);
        }
    }
}
