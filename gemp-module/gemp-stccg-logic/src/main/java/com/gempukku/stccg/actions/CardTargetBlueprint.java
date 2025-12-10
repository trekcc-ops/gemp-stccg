package com.gempukku.stccg.actions;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.game.DefaultGame;


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ReadCardMemoryBlueprint.class, name = "memory"),
        @JsonSubTypes.Type(value = SelectCardTargetBlueprint.class, name = "select")
})
public interface CardTargetBlueprint {

    ActionCardResolver getTargetResolver(DefaultGame cardGame, ActionContext context);

    void addFilter(FilterBlueprint... filterBlueprint);

    boolean canBeResolved(DefaultGame cardGame, ActionContext context);

}