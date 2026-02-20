package com.gempukku.stccg.cards.physicalcard;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public interface ReportableCard extends CardWithCompatibility {

    @JsonProperty("isStopped")
    boolean isStopped();

    @JsonIgnore
    void setAsAboard(PhysicalCard destination);

    @JsonIgnore
    void setAsOnPlanet(PhysicalCard destination);

    @JsonIgnore
    void setAsInSpaceAtLocation(PhysicalCard destination);

    @JsonIgnore
    boolean isAboard(PhysicalCard card);
}