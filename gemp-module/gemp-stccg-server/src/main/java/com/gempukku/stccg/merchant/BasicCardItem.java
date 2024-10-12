package com.gempukku.stccg.merchant;

import com.gempukku.stccg.cards.CardItem;

import java.util.Objects;

public class BasicCardItem implements CardItem {
    private final String blueprintId;

    public BasicCardItem(String blueprintId) {
        this.blueprintId = blueprintId;
    }

    @Override
    public final String getBlueprintId() {
        return blueprintId;
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        CardItem that = (CardItem) obj;

        return Objects.equals(blueprintId, that.getBlueprintId());
    }

    @Override
    public final int hashCode() {
        return blueprintId != null ? blueprintId.hashCode() : 0;
    }
}