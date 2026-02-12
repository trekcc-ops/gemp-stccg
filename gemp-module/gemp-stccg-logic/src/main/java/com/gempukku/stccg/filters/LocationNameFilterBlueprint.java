package com.gempukku.stccg.filters;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.game.DefaultGame;

public class LocationNameFilterBlueprint implements FilterBlueprint {

    @JsonProperty("locationName")
    private final String _locationName;

    public LocationNameFilterBlueprint(@JsonProperty("locationName") String locationName) {
        _locationName = locationName;
    }

    @JsonIgnore
    @Override
    public CardFilter getFilterable(DefaultGame cardGame, ActionContext actionContext) {
        CardFilter filter = new LocationNameFilter(_locationName);
        return filter;
    }
}