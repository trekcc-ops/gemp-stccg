package com.gempukku.stccg.collection;

import com.gempukku.stccg.cards.GenericCardItem;

import java.util.Map;

public interface CardCollection {
    int getCurrency();

    Iterable<GenericCardItem> getAll();

    int getItemCount(String blueprintId);

    Map<String, Object> getExtraInformation();

}
