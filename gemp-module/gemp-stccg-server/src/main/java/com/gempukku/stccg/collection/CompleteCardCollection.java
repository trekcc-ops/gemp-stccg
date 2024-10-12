package com.gempukku.stccg.collection;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.GenericCardItem;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public class CompleteCardCollection implements CardCollection {

    private static final int COMPLETE_COUNT = 4;
    private final CardBlueprintLibrary _library;

    public CompleteCardCollection(CardBlueprintLibrary library) {
        _library = library;
    }
    @Override
    public final int getCurrency() {
        return 0;
    }

    @Override
    public final Iterable<GenericCardItem> getAll() {
        return _library.getBaseCards().keySet().stream().map(blueprintId ->
                GenericCardItem.createItem(blueprintId, COMPLETE_COUNT)).collect(Collectors.toList());
    }

    @Override
    public final int getItemCount(String blueprintId) {
        return _library.getBaseBlueprintId(blueprintId).equals(blueprintId) ? COMPLETE_COUNT : 0;
    }

    @Override
    public final Map<String, Object> getExtraInformation() {
        return Collections.emptyMap();
    }
}