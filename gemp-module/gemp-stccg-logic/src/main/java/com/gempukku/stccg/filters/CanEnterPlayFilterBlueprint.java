package com.gempukku.stccg.filters;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.playcard.PlayCardAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.game.DefaultGame;

public class CanEnterPlayFilterBlueprint implements FilterBlueprint {

    private final PlayCardAction.EnterPlayActionType _actionType;

    public CanEnterPlayFilterBlueprint(@JsonProperty("actionType")
                              PlayCardAction.EnterPlayActionType type) {
        _actionType = type;
    }
    @Override
    public CardFilter getFilterable(DefaultGame cardGame, ActionContext actionContext) {
        return new CanEnterPlayFilter(_actionType);
    }
}