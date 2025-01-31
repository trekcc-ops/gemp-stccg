package com.gempukku.stccg.actions;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gempukku.stccg.cards.ActionContext;


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = SelectCardTargetBlueprint.class, name = "select")
})
public interface CardTargetBlueprint {

    ActionCardResolver getTargetResolver(ActionContext context);

}