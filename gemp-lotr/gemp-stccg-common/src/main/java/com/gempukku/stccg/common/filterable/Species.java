package com.gempukku.stccg.common.filterable;

public enum Species implements Filterable {
    ANDROID("android"),
    BAJORAN("Bajoran"),
    HOLOGRAM("hologram"),
    KLINGON("Klingon");
    private final String _humanReadable;

    Species(String humanReadable) {
        _humanReadable = humanReadable;
    }

    public String getHumanReadable() { return _humanReadable; }

}