package com.gempukku.stccg.filters;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.game.DefaultGame;

@JsonDeserialize(using = FilterBlueprintDeserializer.class)
public interface FilterBlueprint {
    CardFilter getFilterable(DefaultGame cardGame, ActionContext actionContext);
}