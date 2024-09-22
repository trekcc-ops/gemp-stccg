package com.gempukku.stccg.collection;

import com.gempukku.stccg.cards.GenericCardItem;

import java.util.List;

public interface PackBox {
    List<GenericCardItem> openPack();
    List<GenericCardItem> openPack(int selection);
    List<String> GetAllOptions();
}
