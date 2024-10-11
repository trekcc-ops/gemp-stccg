package com.gempukku.stccg.merchant;

import com.gempukku.stccg.cards.CardItem;

import java.util.Objects;

public class BasicCardItem implements CardItem {
    private final String _blueprintId;

    public BasicCardItem(String blueprintId) {
        _blueprintId = blueprintId;
    }

    @Override
    public String getBlueprintId() {
        return _blueprintId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BasicCardItem that = (BasicCardItem) o;

        return Objects.equals(_blueprintId, that._blueprintId);
    }

    @Override
    public int hashCode() {
        return _blueprintId != null ? _blueprintId.hashCode() : 0;
    }
}

