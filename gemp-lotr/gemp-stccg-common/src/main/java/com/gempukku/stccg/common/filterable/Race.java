package com.gempukku.stccg.common.filterable;

public enum Race implements Filterable {
    ORC("Orc"), TROLL("Troll"), HALF_TROLL("Half-troll"), ENT("Ent"),
    SPIDER("Spider"), MAIA("Maia"), GOBLIN("Goblin"),
    DRAGON("Dragon"), EAGLE("Eagle"), BIRD("Bird"), GIANT("Giant"),
    CROW("Crow");

    private final String _humanReadable;

    Race(String humanReadable) {
        _humanReadable = humanReadable;
    }

    public String getHumanReadable() {
        return _humanReadable;
    }

}
