package com.gempukku.stccg.actions.targetresolver;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.filters.AllCardsMatchingFilterResolverBlueprint;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.game.DefaultGame;


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ReadCardMemoryBlueprint.class, name = "memory"),
        @JsonSubTypes.Type(value = ReportCardsResolverBlueprint.class, name = "reportCards"),
        @JsonSubTypes.Type(value = SelectCardTargetBlueprint.class, name = "select"),
        @JsonSubTypes.Type(value = AllCardsMatchingFilterResolverBlueprint.class, name = "selectAll")
})
public interface TargetResolverBlueprint {

    ActionCardResolver getTargetResolver(DefaultGame cardGame, GameTextContext context);

    void addFilter(FilterBlueprint... filterBlueprint);

    boolean canBeResolved(DefaultGame cardGame, GameTextContext context);

}