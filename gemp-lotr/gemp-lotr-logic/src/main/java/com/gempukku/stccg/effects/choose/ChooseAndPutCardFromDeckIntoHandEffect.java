package com.gempukku.stccg.effects.choose;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.Filterable;
import com.gempukku.stccg.common.Zone;
import com.gempukku.stccg.effects.PutCardFromZoneIntoHandEffect;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;

public class ChooseAndPutCardFromDeckIntoHandEffect extends ChooseCardsFromDeckEffect {
    private final Action _action;

    public ChooseAndPutCardFromDeckIntoHandEffect(Action action, String playerId, int minimum, int maximum, Filterable... filters) {
        super(playerId, playerId, minimum, maximum, filters);
        _action = action;
    }

    public ChooseAndPutCardFromDeckIntoHandEffect(Action action, String playerId, String deckId, int minimum, int maximum, Filterable... filters) {
        super(playerId, deckId, minimum, maximum, filters);
        _action = action;
    }

    @Override
    protected void cardsSelected(DefaultGame game, Collection<PhysicalCard> cards) {
        if (cards.size() > 0) {
            SubAction subAction = new SubAction(_action);
            for (PhysicalCard card : cards)
                subAction.appendEffect(new PutCardFromZoneIntoHandEffect(card, Zone.DRAW_DECK, true));
            game.getActionsEnvironment().addActionToStack(subAction);
        }
    }
}
