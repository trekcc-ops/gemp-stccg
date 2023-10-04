package com.gempukku.lotro.effects.choose;

import com.gempukku.lotro.actions.Action;
import com.gempukku.lotro.actions.SubAction;
import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.common.EndOfPile;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.effects.PutCardsFromZoneOnEndOfPileEffect;
import com.gempukku.lotro.game.DefaultGame;

import java.util.Collection;

public class ChooseAndPutCardFromDiscardOnBottomOfDeckEffect extends ChooseCardsFromDiscardEffect {
    private final Action _action;

    public ChooseAndPutCardFromDiscardOnBottomOfDeckEffect(Action action, String playerId, int minimum, int maximum, Filterable... filters) {
        super(playerId, minimum, maximum, filters);
        _action = action;
    }

    @Override
    protected void cardsSelected(DefaultGame game, Collection<PhysicalCard> cards) {
        if (cards.size() > 0) {
            SubAction subAction = new SubAction(_action);
            for (PhysicalCard card : cards)
                subAction.appendEffect(new PutCardsFromZoneOnEndOfPileEffect(true, Zone.DISCARD, Zone.DRAW_DECK, EndOfPile.BOTTOM, card));
            game.getActionsEnvironment().addActionToStack(subAction);
        }
    }
}
