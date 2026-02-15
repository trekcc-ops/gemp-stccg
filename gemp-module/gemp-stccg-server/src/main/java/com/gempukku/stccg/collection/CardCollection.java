package com.gempukku.stccg.collection;

import com.gempukku.stccg.cards.GenericCardItem;

import java.util.Iterator;
import java.util.Map;

public interface CardCollection extends Iterable<GenericCardItem> {
    int getCurrency();

    Iterable<GenericCardItem> getAll();

    int getItemCount(String blueprintId);

    Map<String, Object> getExtraInformation();

    @Override
    default Iterator<GenericCardItem> iterator() {
        return getAll().iterator();
    }
}