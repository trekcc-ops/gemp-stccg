package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.PutCardsFromZoneOnEndOfPileEffect;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.EndOfPile;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;

public class ChooseAndPutCardFromDiscardOnBottomOfDeckEffect extends ChooseCardsFromZoneEffect {
    private final Action _action;

    public ChooseAndPutCardFromDiscardOnBottomOfDeckEffect(DefaultGame game, Action action, String playerId, int minimum, int maximum, Filterable... filters) {
        super(game, Zone.DISCARD, playerId, minimum, maximum, filters);
        _action = action;
    }

    @Override
    protected void cardsSelected(DefaultGame game, Collection<PhysicalCard> cards) {
        if (!cards.isEmpty()) {
            SubAction subAction = new SubAction(_action, _game);
            for (PhysicalCard card : cards)
                subAction.appendEffect(new PutCardsFromZoneOnEndOfPileEffect(game,true, Zone.DISCARD, Zone.DRAW_DECK, EndOfPile.BOTTOM, card));
            game.getActionsEnvironment().addActionToStack(subAction);
        }
    }
}