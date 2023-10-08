package com.gempukku.stccg.cards;

import com.gempukku.stccg.cards.CardCollection;

import java.util.List;

public interface PackBox {
    List<CardCollection.Item> openPack();
    List<CardCollection.Item> openPack(int selection);
    List<String> GetAllOptions();
}
