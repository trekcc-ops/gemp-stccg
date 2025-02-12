package com.gempukku.stccg.actions;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.filters.FilterBlueprint;


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = SelectCardTargetBlueprint.class, name = "select")
})
public interface CardTargetBlueprint {

    ActionCardResolver getTargetResolver(ActionContext context);

    void addFilter(FilterBlueprint... filterBlueprint);

}