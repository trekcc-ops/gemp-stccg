package com.gempukku.stccg.common.filterable;

public enum GameType {
    FIRST_EDITION("First Edition"),
    SECOND_EDITION("Second Edition"),
    TRIBBLES("Tribbles");

    private String _humanReadable;

    GameType(String humanReadable) {
        _humanReadable = humanReadable;
    }

    public String getHumanReadable() {
        return _humanReadable;
    }

}