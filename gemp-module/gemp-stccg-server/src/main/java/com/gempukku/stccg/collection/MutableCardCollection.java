package com.gempukku.stccg.collection;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.packs.ProductLibrary;

import java.util.Map;

public interface MutableCardCollection extends CardCollection {
    void addItem(String itemId, int count);

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean removeItem(String itemId, int count);

    void addCurrency(int currency);

    boolean removeCurrency(int currency);

    CardCollection openPack(String packId, String selection, CardBlueprintLibrary cardLibrary,
                            ProductLibrary productLibrary);

    void setExtraInformation(Map<String, Object> extraInformation);


}