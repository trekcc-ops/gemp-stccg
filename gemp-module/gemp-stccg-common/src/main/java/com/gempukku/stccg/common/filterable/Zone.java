package com.gempukku.stccg.common.filterable;

public enum Zone implements Filterable {
    DRAW_DECK("draw deck", false, false, false, true),
    MISSIONS_PILE("missions pile", false, true, false, true),
    SEED_DECK("seed deck", false, true, false, true),
    CORE("table", true, true, true, true),
    @SuppressWarnings("SpellCheckingInspection")
    SPACELINE("spaceline", true, true, true, false),
    AT_LOCATION("location", true, true, true, false),

    ATTACHED("play", true, true, true, false),

    REMOVED("removed", true, true, false, true),
    POINT_AREA("point area", true, true, false, true),

    PLAY_PILE("play pile", true, true,true, true),

    // Private knowledge
    HAND("hand", false, true, false, true),
    DISCARD("discard", true, true, false, true),

    // Nobody sees
    VOID("void", false, false, false, false);

    private final String _humanReadable;
    private final boolean _public;
    private final boolean _visibleByOwner;
    private final boolean _inPlay;
    private final boolean _hasList;

    Zone(String humanReadable, boolean isPublic, boolean visibleByOwner, boolean inPlay, boolean hasList) {
        _humanReadable = humanReadable;
        _public = isPublic;
        _visibleByOwner = visibleByOwner;
        _inPlay = inPlay;
        _hasList = hasList; // if hasList is true, GameState has a list of PhysicalCards to represent the zone
    }

    public String getHumanReadable() {
        return _humanReadable;
    }

    public boolean isInPlay() {
        return _inPlay;
    }

    public boolean isPublic() {
        return _public;
    }

    public boolean isVisibleByOwner() {
        return _visibleByOwner;
    }

    public boolean hasList() { return _hasList; }
}