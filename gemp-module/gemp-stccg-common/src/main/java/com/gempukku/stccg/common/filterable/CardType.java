package com.gempukku.stccg.common.filterable;

import com.fasterxml.jackson.annotation.JsonValue;

@SuppressWarnings("unused")
public enum CardType implements Filterable {
    ARTIFACT, EVENT, SITE,
    DILEMMA, DOORWAY, EQUIPMENT, FACILITY, INCIDENT, INTERRUPT, MISSION, OBJECTIVE, PERSONNEL, SHIP,
    TACTIC, TIME_LOCATION, TRIBBLE, TROUBLE;

    @JsonValue
    public String getJsonValue() {
        return name().replace("_","");
    }
}