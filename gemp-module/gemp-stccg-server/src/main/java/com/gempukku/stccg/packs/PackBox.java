package com.gempukku.stccg.packs;

import com.gempukku.stccg.cards.GenericCardItem;

import java.util.List;

public interface PackBox {
    List<GenericCardItem> openPack();
    List<GenericCardItem> openPack(int selection);
}