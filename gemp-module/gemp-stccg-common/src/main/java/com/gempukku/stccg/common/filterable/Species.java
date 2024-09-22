package com.gempukku.stccg.common.filterable;

import org.apache.commons.lang.StringUtils;

public enum Species implements Filterable {
    ANDROID("android"),
    BAJORAN, BENZITE,
    HOLOGRAM("hologram"),
    HUMAN("human"),
    KLINGON, MARKALIAN, ROMULAN, VULCAN;
    private final String _humanReadable;

    Species() { _humanReadable = StringUtils.capitalize(name().toLowerCase()); }
    Species(String humanReadable) {
        _humanReadable = humanReadable;
    }

    public String getHumanReadable() { return _humanReadable; }

}