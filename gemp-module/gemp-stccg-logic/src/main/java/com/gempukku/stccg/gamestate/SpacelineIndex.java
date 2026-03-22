package com.gempukku.stccg.gamestate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gempukku.stccg.common.filterable.Quadrant;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = LocationSpacelineIndex.class, name = "location"),
        @JsonSubTypes.Type(value = NonLocationSpacelineIndex.class, name = "card")
})
public interface SpacelineIndex {
    @JsonProperty("quadrant")
    public Quadrant getQuadrant();
}