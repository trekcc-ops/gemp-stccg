package com.gempukku.stccg.cards.physicalcard;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface ReportableCard extends CardWithCompatibility {

    @JsonProperty("isStopped")
    boolean isStopped();

}