package com.gempukku.stccg.filters;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.playcard.EnterPlayActionType;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.game.DefaultGame;

public class CanEnterPlayFilterBlueprint implements FilterBlueprint {

    private final EnterPlayActionType _actionType;

    public CanEnterPlayFilterBlueprint(@JsonProperty("actionType")
                                       EnterPlayActionType type) {
        _actionType = type;
    }
    @Override
    public CardFilter getFilterable(DefaultGame cardGame, GameTextContext actionContext) {
        return new CanEnterPlayFilter(_actionType);
    }
}