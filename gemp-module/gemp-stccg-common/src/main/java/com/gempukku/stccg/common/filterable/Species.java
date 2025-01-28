package com.gempukku.stccg.common.filterable;

import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang.StringUtils;

import java.util.Locale;

@SuppressWarnings({"unused", "SpellCheckingInspection"})
public enum Species implements Filterable {
    ANDROID("android"),
    BAJORAN, BENZITE, BOLIAN, FERENGI, HEKARAN,
    HOLOGRAM("hologram"),
    HUMAN("human"),
    KLINGON, MARKALIAN, NAPEAN, ROMULAN, VULCAN;
    private final String _humanReadable;

    Species() { _humanReadable = StringUtils.capitalize(name().toLowerCase(Locale.ROOT)); }
    Species(String humanReadable) {
        _humanReadable = humanReadable;
    }

    @JsonValue
    private String getHumanReadable() {
        return _humanReadable;
    }

}