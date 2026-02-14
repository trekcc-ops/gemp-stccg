package com.gempukku.stccg.gamestate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.common.filterable.Quadrant;

public class LocationSpacelineIndex implements SpacelineIndex {

    private final int _locationId;

    private final Quadrant _quadrant;

    @JsonCreator
    public LocationSpacelineIndex(
            @JsonProperty("locationId")
            int locationId,
            @JsonProperty("quadrant")
            Quadrant quadrant) {
        _locationId = locationId;
        _quadrant = quadrant;
    }

    public LocationSpacelineIndex(MissionLocation location) {
        _locationId = location.getLocationId();
        _quadrant = location.getQuadrant();
    }

    @Override
    public Quadrant getQuadrant() {
        return _quadrant;
    }

    @JsonProperty("locationId")
    public int getLocationId() {
        return _locationId;
    }
}