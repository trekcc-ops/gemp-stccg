package com.gempukku.stccg.packs;

import com.gempukku.stccg.game.CardCollection;

import java.util.List;

public interface PackBox {
    List<CardCollection.Item> openPack();
    List<CardCollection.Item> openPack(int selection);
    List<String> GetAllOptions();
}
