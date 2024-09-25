package com.gempukku.stccg.common.filterable;

public enum Characteristic implements Filterable {
    K_EHLEYR("K'Ehleyr");

    private final String _humanReadable;

    Characteristic(String humanReadable) {
        _humanReadable = humanReadable;
    }

    public String getHumanReadable() { return _humanReadable; }
}