package com.gempukku.stccg.common.filterable;

public enum CardAttribute implements Filterable {
    RANGE(false),
    WEAPONS(false),
    SHIELDS(false),
    INTEGRITY(true),
    CUNNING(true),
    STRENGTH(true);

    private final boolean _isPersonnelAttribute;

    CardAttribute(boolean isPersonnelAttribute) {
        _isPersonnelAttribute = isPersonnelAttribute;
    }

    public boolean isPersonnelAttribute() {
        return _isPersonnelAttribute;
    }
}