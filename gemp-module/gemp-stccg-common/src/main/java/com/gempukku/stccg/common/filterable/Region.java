package com.gempukku.stccg.common.filterable;

import com.fasterxml.jackson.annotation.JsonValue;

@SuppressWarnings({"SpellCheckingInspection", "unused"})
public enum Region implements Filterable {
    ARGOLIS_CLUSTER, BADLANDS, BAJOR, BRIAR_PATCH, CARDASSIA, CETI_ALPHA, CHIN_TOKA,
    DELPHIC_EXPANSE, DEMILITARIZED_ZONE, GREAT_BARRIER, MCALLISTER, MURASAKI, MUTARA, NEKRIT_EXPANSE, NEUTRAL_ZONE,
    NORTHWEST_PASSAGE, ROMULUS_SYSTEM, SECTOR_001, TELLUN, VALO,

    //2E regions
    QO_NOS_SYSTEM;

    @JsonValue
    public String getJsonValue() {
        return switch(this) {
            case QO_NOS_SYSTEM -> "Qo'Nos System";
            default -> name().replace("_", " ");
        };
    }
}