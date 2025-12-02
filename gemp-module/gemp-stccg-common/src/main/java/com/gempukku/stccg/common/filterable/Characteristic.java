package com.gempukku.stccg.common.filterable;

@SuppressWarnings("unused")
public enum Characteristic implements Filterable {
    ADMIRAL("admiral"),
    COOK("cook"),
    GENERAL("general"),

    @SuppressWarnings("SpellCheckingInspection") K_EHLEYR("K'Ehleyr"),
    MAJE("Maje"),
    NOR("Nor"),
    SCOTTY("Scotty");

    private String _humanReadable;

    Characteristic(String humanReadable) {
        _humanReadable = humanReadable;
    }

    public String getHumanReadable() {
        return _humanReadable;
    }

}