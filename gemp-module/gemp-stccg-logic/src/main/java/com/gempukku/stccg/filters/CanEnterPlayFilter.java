package com.gempukku.stccg.filters;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.playcard.EnterPlayActionType;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class CanEnterPlayFilter implements CardFilter {

    private final EnterPlayActionType _actionType;

    public CanEnterPlayFilter(@JsonProperty("actionType")
                              EnterPlayActionType type) {
        _actionType = type;
    }

    @Override
    public boolean accepts(DefaultGame game, PhysicalCard card) {
        return game.getRules().cardCanEnterPlay(game, card, _actionType);
    }

}