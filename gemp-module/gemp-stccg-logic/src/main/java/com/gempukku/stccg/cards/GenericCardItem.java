package com.gempukku.stccg.cards;

import com.gempukku.stccg.common.CardItemType;
import com.gempukku.stccg.common.filterable.SubDeck;

import java.util.Objects;

public class GenericCardItem implements CardItem {

    private final CardItemType _type;
    private final int _count;
    private final String _blueprintId;
    private final boolean _recursive;
    private final SubDeck _subDeck;

    private GenericCardItem(CardItemType type, int count, String blueprintId, boolean recursive) {
        this(type, count, blueprintId, recursive, null);
    }

    private GenericCardItem(CardItemType type, int count, String blueprintId, boolean recursive, SubDeck subDeck) {
        _type = type;
        _count = count;
        _blueprintId = blueprintId;
        _recursive = recursive;
        _subDeck = subDeck;
    }

    public static GenericCardItem createItem(String blueprintId, int count) {
        return createItem(blueprintId, count, false);
    }

    public static GenericCardItem createItem(String blueprintId, int count, boolean recursive) {
        if (blueprintId.startsWith("(S)"))
            return new GenericCardItem(CardItemType.SELECTION, count, blueprintId, recursive);
        else if (!blueprintId.contains("_"))
            return new GenericCardItem(CardItemType.PACK, count, blueprintId, recursive);
        else
            return new GenericCardItem(CardItemType.CARD, count, blueprintId, recursive);
    }

    public static GenericCardItem createItem(String blueprintId, int count, SubDeck subDeck) {
        return new GenericCardItem(CardItemType.CARD, count, blueprintId, false, subDeck);
    }

    public static GenericCardItem createItem(String combined) {
        String[] result = combined.split("x", 2);
        return createItem(result[1], Integer.parseInt(result[0]));
    }

    public CardItemType getType() {
        return _type;
    }

    public int getCount() {
        return _count;
    }

    @Override
    public String getBlueprintId() {
        return _blueprintId;
    }

    public String getSubDeckString() {
        if (_subDeck == null)
            return null;
        else return _subDeck.name();
    }

    public boolean isRecursive() {
        return _recursive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        GenericCardItem item = (GenericCardItem) o;

        if (_count != item._count)
            return false;
        if (!Objects.equals(_blueprintId, item._blueprintId))
            return false;
        return _type == item._type;
    }

    @Override
    public int hashCode() {
        int result = _type != null ? _type.hashCode() : 0;
        result = 31 * result + _count;
        result = 31 * result + (_blueprintId != null ? _blueprintId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return _count + "x" + _blueprintId;
    }
}
