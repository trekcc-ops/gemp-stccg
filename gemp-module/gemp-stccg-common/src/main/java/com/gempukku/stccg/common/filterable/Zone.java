package com.gempukku.stccg.common.filterable;

public enum Zone implements Filterable {
    DRAW_DECK(false, false, false, true),
    MISSIONS_PILE(false, true, false, true),
    SEED_DECK_FOR_DILEMMA_PHASE(false, true, false, true),
    SEED_DECK_OTHER(false, true, false, true),
    CORE(true, true, true, true),
    @SuppressWarnings("SpellCheckingInspection")
    SPACELINE(true, true, true, false),
    AT_LOCATION(true, true, true, false),

    ATTACHED(true, true, true, false),

    REMOVED(true, true, false, true),
    POINT_AREA(true, true, false, true),

    PLAY_PILE(true, true,true, true),

    // Private knowledge
    HAND(false, true, false, true),
    DISCARD(true, true, false, true),

    // Nobody sees
    VOID(false, false, false, false);

    private final boolean _public;
    private final boolean _visibleByOwner;
    private final boolean _inPlay;
    private final boolean _hasList;

    Zone(boolean isPublic, boolean visibleByOwner, boolean inPlay, boolean hasList) {
        _public = isPublic;
        _visibleByOwner = visibleByOwner;
        _inPlay = inPlay;
        _hasList = hasList; // if hasList is true, GameState has a list of PhysicalCards to represent the zone
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