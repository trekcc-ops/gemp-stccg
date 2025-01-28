package com.gempukku.stccg.common.filterable;

import com.fasterxml.jackson.annotation.JsonValue;

@SuppressWarnings("unused")
public enum PropertyLogo implements Filterable {
    // TV shows
    TOS_LOGO,
    TNG_LOGO,
    DS9_LOGO,
    VOY_LOGO,
    ENT_LOGO,
    TAS_LOGO,

    // Movies
    TMP_LOGO,
    ST2_LOGO,
    ST3_LOGO,
    ST4_LOGO,
    ST5_LOGO,
    ST6_LOGO,
    GENERATIONS_LOGO,
    FIRST_CONTACT_LOGO,
    INSURRECTION_LOGO,
    NEMESIS_LOGO,

    // Other products
    ARMADA_LOGO,
    KLINGON_CHALLENGE_LOGO,
    CCG_LOGO;

    @JsonValue
    public String getJsonValue() {
        return name().replace("_","-");
    }

}