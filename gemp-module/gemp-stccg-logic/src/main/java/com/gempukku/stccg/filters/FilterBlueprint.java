package com.gempukku.stccg.filters;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.common.filterable.Filterable;

@JsonDeserialize(using = FilterBlueprintDeserializer.class)
public interface FilterBlueprint {
    Filterable getFilterable(ActionContext actionContext);

}