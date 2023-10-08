package com.gempukku.stccg.effects.choose;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.Filterable;
import com.gempukku.stccg.common.Zone;
import com.gempukku.stccg.effects.PutCardFromZoneIntoHandEffect;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;

public class ChooseAndPutCardFromDiscardIntoHandEffect extends ChooseCardsFromDiscardEffect {
    private final Action _action;

    public ChooseAndPutCardFromDiscardIntoHandEffect(Action action, String playerId, int minimum, int maximum, Filterable... filters) {
        super(playerId, minimum, maximum, filters);
        _action = action;
    }

    @Override
    protected void cardsSelected(DefaultGame game, Collection<PhysicalCard> cards) {
        if (cards.size() > 0) {
            SubAction subAction = new SubAction(_action);
            for (PhysicalCard card : cards)
                subAction.appendEffect(new PutCardFromZoneIntoHandEffect(card, Zone.DISCARD));
            game.getActionsEnvironment().addActionToStack(subAction);
        }
    }
}
