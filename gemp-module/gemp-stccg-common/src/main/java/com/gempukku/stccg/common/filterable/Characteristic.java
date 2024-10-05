package com.gempukku.stccg.common.filterable;

public enum Characteristic implements Filterable {
    COOK("cook"),
    K_EHLEYR("K'Ehleyr");

    private final String _humanReadable;

    Characteristic(String humanReadable) {
        _humanReadable = humanReadable;
    }

}