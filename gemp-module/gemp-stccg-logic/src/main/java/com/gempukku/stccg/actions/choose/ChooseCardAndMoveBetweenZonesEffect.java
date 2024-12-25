package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.placecard.PutCardFromZoneIntoHandEffect;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;

public class ChooseCardAndMoveBetweenZonesEffect extends ChooseCardsFromZoneEffect {
    private final Action _action;
    private final Zone _toZone;
    private final boolean _reveal;

    public ChooseCardAndMoveBetweenZonesEffect(DefaultGame game, Zone fromZone, Zone toZone, Action action,
                                               String playerId, int minimum, int maximum, Filterable... filters) {
        super(game, fromZone, playerId, playerId, minimum, maximum, filters);
        _action = action;
        _toZone = toZone;
        _reveal = _fromZone != Zone.DISCARD;
    }

    @Override
    protected void cardsSelected(DefaultGame game, Collection<PhysicalCard> cards) {
        // ChooseAndPutCardsFromDeckIntoHandEffect
        // ChooseAndPutCardFromDiscardIntoHandEffect
        if (!cards.isEmpty() && _toZone == Zone.HAND) {
            SubAction subAction = new SubAction(_action, _game);
            for (PhysicalCard card : cards)
                subAction.appendEffect(new PutCardFromZoneIntoHandEffect(game, card, _fromZone, _reveal));
            game.getActionsEnvironment().addActionToStack(subAction);
        }
    }
}