package com.gempukku.stccg.filters;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.playcard.PlayCardAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class CanEnterPlayFilter implements CardFilter {

    private final PlayCardAction.EnterPlayActionType _actionType;

    public CanEnterPlayFilter(@JsonProperty("actionType")
                              PlayCardAction.EnterPlayActionType type) {
        _actionType = type;
    }

    @Override
    public boolean accepts(DefaultGame game, PhysicalCard card) {
        return game.getRules().cardCanEnterPlay(game, card, _actionType);
    }

}